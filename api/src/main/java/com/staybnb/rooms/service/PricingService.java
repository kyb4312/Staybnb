package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Pricing;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.dto.request.SearchPricingRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.vo.DateRange;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.exception.InvalidDateRangeException;
import com.staybnb.rooms.repository.PricingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
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

    /**
     * 선택 구간들 숙박 가격 변경
     */
    @Transactional
    public void updateSelectedDatesPricing(Long roomId, UpdatePricingRequest request) {
        Room room = roomService.findById(roomId);
        validateDateSelected(request.getDateSelected());

        request.getDateSelected().forEach(range ->
                updatePricing(room, range.getStartDate(), range.getEndDate().plusDays(1), request.getPricePerNight()));
    }

    /**
     * startDate ~ endDate 구간 숙박 가격 변경
     */
    private void updatePricing(Room room, LocalDate startDateInclusive, LocalDate endDateInclusive, int pricePerNight) {
        updateExistingPricing(room, startDateInclusive, endDateInclusive);
        pricingRepository.save(new Pricing(room, startDateInclusive, endDateInclusive, pricePerNight));
    }

    /**
     * 날짜가 겹치는 기존 pricing 데이터가 있을 경우,
     * 기존 데이터는 삭제하고 겹치지 않는 구간 데이터만 다시 저장
     */
    private void updateExistingPricing(Room room, LocalDate startDateInclusive, LocalDate endDateExclusive) {
        List<Pricing> conflictedPricingList = pricingRepository.findPricingsByDate(room.getId(), startDateInclusive, endDateExclusive);
        pricingRepository.deleteAll(conflictedPricingList);
        pricingRepository.flush();

        for (Pricing conflicted : conflictedPricingList) {
            if (conflicted.getStartDate().isBefore(startDateInclusive)) {
                pricingRepository.save(new Pricing(room, conflicted.getStartDate(), startDateInclusive, conflicted.getPricePerNight()));
            }
            if (conflicted.getEndDate().isAfter(endDateExclusive)) {
                pricingRepository.save(new Pricing(room, endDateExclusive, conflicted.getEndDate(), conflicted.getPricePerNight()));
            }
        }
    }

    public List<Pricing> findPricingsByMonth(Long roomId, YearMonth yearMonth) {
        return pricingRepository.findPricingsByDate(roomId, yearMonth.atDay(1), yearMonth.plusMonths(1).atDay(1));
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

    private void validateDateSelected(List<DateRange> dateSelected) {
        dateSelected.forEach(dateRange -> {
            if (dateRange.getStartDate().isBefore(LocalDate.now())) {
                throw new InvalidDateRangeException("startDate가 과거 일자입니다.", dateRange.getStartDate(), LocalDate.now());
            }
            if (dateRange.getStartDate().isAfter(dateRange.getEndDate())) {
                throw new InvalidDateRangeException("startDate는 endDate 보다 같거나 이전 일자여야 합니다.", dateRange.getStartDate(), dateRange.getEndDate());
            }
            if (!dateRange.getEndDate().isBefore(LocalDate.now().plusYears(1))) {
                throw new InvalidDateRangeException("1년 이내의 가격만 설정 가능합니다.", dateRange.getStartDate(), dateRange.getEndDate());
            }
        });
    }
}
