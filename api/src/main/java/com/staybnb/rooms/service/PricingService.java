package com.staybnb.rooms.service;

import com.staybnb.common.exception.custom.UnauthorizedException;
import com.staybnb.rooms.domain.Pricing;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.dto.request.SearchPricingRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.vo.DateRange;
import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.common.exception.custom.InvalidDateRangeException;
import com.staybnb.rooms.repository.PricingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRepository pricingRepository;

    private final RoomService roomService;
    private final ExchangeRateService exchangeRateService;

    /**
     * 숙박 총 가격 조회
     * @return PricingResponse
     */
    public PricingResponse getTotalPricing(Long roomId, SearchPricingRequest request) {
        Room room = roomService.findById(roomId);
        validateDateRange(request);

        double totalPrice = getTotalPrice(room, request.getStartDate(), request.getEndDate(), Currency.valueOf(request.getCurrency()));

        return new PricingResponse(roomId, request.getStartDate(), request.getEndDate(), totalPrice, request.getCurrency()
        );
    }

    /**
     * 숙박 총 가격 조회
     * @return double
     */
    public double getTotalPrice(Room room, LocalDate checkInDateInclusive, LocalDate checkOutDateExclusive, Currency currency) {
        return exchangeRateService.convert(
                room.getCurrency(),
                currency,
                calcTotalPrice(room, checkInDateInclusive, checkOutDateExclusive)
        );
    }

    /**
     * [startDate, endDate] 기간 총 숙박 가격 계산
     */
    private int calcTotalPrice(Room room, LocalDate checkInDateInclusive, LocalDate checkOutDateExclusive) {
        int totalDays = (int) ChronoUnit.DAYS.between(checkInDateInclusive, checkOutDateExclusive);
        int totalPrice = 0;

        List<Pricing> pricingList = pricingRepository.findPricingsByDate(room.getId(), checkInDateInclusive, checkOutDateExclusive);

        for (Pricing pricing : pricingList) {
            int days = countDaysWithinRange(pricing, checkInDateInclusive, checkOutDateExclusive);
            totalPrice += days * pricing.getPricePerNight();
            totalDays -= days;
        }

        totalPrice += totalDays * room.getBasePrice();

        return totalPrice;
    }

    /**
     * pricing 날짜 범위 중 [startDate, endDate] 구간에 포함되는 일수 카운트
     */
    private int countDaysWithinRange(Pricing pricing, LocalDate startDateInclusive, LocalDate endDateExclusive) {
        return (int) ChronoUnit.DAYS.between(
                pricing.getStartDate().isBefore(startDateInclusive) ? startDateInclusive : pricing.getStartDate(),
                pricing.getEndDate().isAfter(endDateExclusive) ? endDateExclusive : pricing.getEndDate()
        );
    }

    @Transactional
    public void updateSelectedDatesPricing(long userId, long roomId, UpdatePricingRequest request) {
        Room room = roomService.findById(roomId);
        validateUser(userId, room);
        DateRangeRequest.sortAndValidateDateSelected(request.getDateSelected());

        // DateRangeRequest는 endDate가 exclusive인 DateRange로 변경 후 전달
        updatePricing(room,
                request.getDateSelected().stream().map(DateRangeRequest::toDateRange).toList(),
                request.getPricePerNight());
    }

    /**
     * dateRanges 날짜 범위에 대한 availability 데이터 추가.
     * dateRanges 전체 날짜 범위와 겹치는 기존 availability 데이터가 있을 경우, 기존 데이터는 삭제하고 겹치지 않는 구간 데이터만 다시 저장
     */
    private void updatePricing(Room room, List<DateRange> sortedSelectedDateRanges, int pricePerNight) {
        LocalDate minStartDate = sortedSelectedDateRanges.getFirst().getStartDate();
        LocalDate maxEndDate = sortedSelectedDateRanges.getLast().getEndDate();

        // 업데이트 할 dateRanges 구간과 겹치는 데이터 조회
        List<Pricing> sortedConflictedPricings = pricingRepository
                .findOrderedPricingsByDate(room.getId(), minStartDate, maxEndDate);

        // 충돌 하는 범위 내 데이터 전체 삭제
        if (!sortedConflictedPricings.isEmpty()) {
            pricingRepository.deleteAll(sortedConflictedPricings);
            pricingRepository.flush();
        }

        // 새로 요청 온 날짜 구간(dateRanges)과 기존 데이터 중 겹치지 않는 구간의 데이터 저장
        List<Pricing> newPricings = new ArrayList<>();

        for (DateRange range : sortedSelectedDateRanges) {
            newPricings.add(new Pricing(room, range.getStartDate(), range.getEndDate(), pricePerNight));
        }

        addNonOverlappingRanges(newPricings, room, sortedConflictedPricings, sortedSelectedDateRanges);

        pricingRepository.saveAll(newPricings);
    }

    /**
     * sortedConflicted 범위 중 sortedSelected 와 겹치지 않는 구간을 newPricings 리스트에 추가
     */
    private void addNonOverlappingRanges(List<Pricing> newPricings, Room room,
                                         List<Pricing> sortedConflicted, List<DateRange> sortedSelected) {
        int selectedIndex = 0;
        LocalDate currentStart = null;
        LocalDate currentEnd = null;

        for (Pricing conflicted : sortedConflicted) {
            currentStart = conflicted.getStartDate();
            currentEnd = conflicted.getEndDate();

            // 현재 conflicted와 겹치는 selected 들을 모두 처리
            while (selectedIndex < sortedSelected.size()) {
                DateRange selected = sortedSelected.get(selectedIndex);

                // selected가 conflicted 보다 완전히 앞에 있으면 다음 selected 로
                if (selected.getEndDate().isBefore(currentStart) || selected.getEndDate().isEqual(currentStart)) {
                    selectedIndex++;
                    continue;
                }

                // 안 겹치는 conflicted 앞 부분 복원
                if (currentStart.isBefore(selected.getStartDate())) {
                    newPricings.add(new Pricing(room, currentStart, selected.getStartDate(), conflicted.getPricePerNight()));
                }
                currentStart = selected.getEndDate();

                // currentStart가 currentEnd와 같거나 더 이후라면, 현재 conflicted에 대한 처리를 완료한 것
                if (!currentStart.isBefore(currentEnd)) {
                    break;
                }
            }
        }

        // 마지막 conflicted 뒷 부분 복원
        if (currentStart != null && currentStart.isBefore(currentEnd)) {
            newPricings.add(new Pricing(room, currentStart, currentEnd, sortedConflicted.getLast().getPricePerNight()));
        }
    }

    public List<Pricing> findPricingsByMonth(Long roomId, YearMonth yearMonth) {
        return pricingRepository.findPricingsByDate(roomId, yearMonth.atDay(1), yearMonth.plusMonths(1).atDay(1));
    }

    private void validateUser(long userId, Room room) {
        if (!room.getHost().getId().equals(userId)) {
            throw new UnauthorizedException(userId);
        }
    }

    private void validateDateRange(SearchPricingRequest request) {
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException("startDate가 과거 일자입니다.", request.getStartDate(), LocalDate.now());
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new InvalidDateRangeException("startDate는 endDate 보다 이전 일자여야 합니다.", request.getStartDate(), request.getEndDate());
        }
        if (!request.getEndDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new InvalidDateRangeException("1년 이내의 가격만 조회 가능합니다.", request.getStartDate(), request.getEndDate());
        }
    }

}
