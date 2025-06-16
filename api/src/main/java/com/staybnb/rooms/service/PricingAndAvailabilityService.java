package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Availability;
import com.staybnb.rooms.domain.Currency;
import com.staybnb.rooms.domain.Pricing;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.request.SearchPricingRequest;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.vo.DateRange;
import com.staybnb.rooms.dto.response.CalendarResponse;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.dto.response.vo.DailyInfo;
import com.staybnb.rooms.exception.InvalidDateRangeException;
import com.staybnb.rooms.exception.InvalidRoomIdException;
import com.staybnb.rooms.exception.InvalidYearMonthException;
import com.staybnb.rooms.repository.AvailabilityRepository;
import com.staybnb.rooms.repository.PricingRepository;
import com.staybnb.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PricingAndAvailabilityService {

    private final PricingRepository pricingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final RoomRepository roomRepository;
    private final CurrencyService currencyService;

    /**
     * 숙박 총 가격 조회
     */
    public PricingResponse getPricing(Long roomId, SearchPricingRequest request) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new InvalidRoomIdException(roomId));
        validateDateRange(request);

        double totalPrice = currencyService.convert(
                room.getCurrency(),
                currencyService.getByCode(request.getCurrency()),
                calcTotalPrice(room, request.getStartDate(), request.getEndDate().minusDays(1))
        );

        return new PricingResponse(roomId, request.getStartDate(), request.getEndDate(), totalPrice, request.getCurrency()
        );
    }

    /**
     * [startDate, endDate] 기간 총 숙박 가격 계산
     */
    private int calcTotalPrice(Room room, LocalDate startDate, LocalDate endDate) {
        int totalDays = countDays(startDate, endDate);
        int totalPrice = 0;

        List<Pricing> pricingList = pricingRepository.findPricingsByDate(room.getId(), startDate, endDate);

        for (Pricing pricing : pricingList) {
            int days = countDaysWithinRange(pricing, startDate, endDate);
            totalPrice += days * pricing.getPricePerNight();
            totalDays -= days;
        }

        totalPrice += totalDays * room.getBasePrice();

        return totalPrice;
    }

    /**
     * [(startDate, endDate] 일수 카운트
     */
    private int countDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * pricing 날짜 범위 중 [startDate, endDate] 구간에 포함되는 일수 카운트
     */
    private int countDaysWithinRange(Pricing pricing, LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(
                pricing.getStartDate().isBefore(startDate) ? startDate : pricing.getStartDate(),
                pricing.getEndDate().isAfter(endDate) ? endDate : pricing.getEndDate()
        ) + 1;
    }

    /**
     * 선택 구간들 숙박 가격 변경
     */
    @Transactional
    public void updateSelectedDatesPricing(Long roomId, UpdatePricingRequest request) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new InvalidRoomIdException(roomId));
        validateDateSelected(request.getDateSelected());

        request.getDateSelected().forEach(range ->
                updatePricing(room, range.getStartDate(), range.getEndDate(), request.getPricePerNight()));
    }

    /**
     * startDate ~ endDate 구간 숙박 가격 변경
     */
    private void updatePricing(Room room, LocalDate startDate, LocalDate endDate, int pricePerNight) {
        updateExistingPricing(room, startDate, endDate);
        pricingRepository.save(new Pricing(room, startDate, endDate, pricePerNight));
    }

    /**
     * 날짜가 겹치는 기존 pricing 데이터가 있을 경우,
     * 기존 데이터는 삭제하고 겹치지 않는 구간 데이터만 다시 저장
     */
    private void updateExistingPricing(Room room, LocalDate startDate, LocalDate endDate) {
        List<Pricing> conflictedPricingList = pricingRepository.findPricingsByDate(room.getId(), startDate, endDate);
        pricingRepository.deleteAll(conflictedPricingList);

        for (Pricing conflicted : conflictedPricingList) {
            if (conflicted.getStartDate().isBefore(startDate)) {
                pricingRepository.save(new Pricing(room, conflicted.getStartDate(), startDate.minusDays(1), conflicted.getPricePerNight()));
            }
            if (conflicted.getEndDate().isAfter(endDate)) {
                pricingRepository.save(new Pricing(room, endDate.plusDays(1), conflicted.getEndDate(), conflicted.getPricePerNight()));
            }
        }
    }

    /**
     * 선택 구간들 숙박 가능 여부 변경
     */
    @Transactional
    public void updateSelectedDatesAvailability(long roomId, UpdateAvailabilityRequest request) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new InvalidRoomIdException(roomId));
        validateDateSelected(request.getDateSelected());

        request.getDateSelected().forEach(range ->
                updateAvailability(room, range.getStartDate(), range.getEndDate(), request.getIsAvailable()));
    }

    /**
     * startDate ~ endDate 구간 숙박 가능 여부 변경
     */
    private void updateAvailability(Room room, LocalDate startDate, LocalDate endDate, boolean isAvailable) {
        updateConflictedAvailability(room, startDate, endDate);
        availabilityRepository.save(new Availability(room, startDate, endDate, isAvailable));
    }

    /**
     * 날짜가 겹치는 기존 availability 데이터가 있을 경우,
     * 기존 데이터는 삭제하고 겹치지 않는 구간 데이터만 다시 저장
     */
    private void updateConflictedAvailability(Room room, LocalDate startDate, LocalDate endDate) {
        List<Availability> conflictedAvailabilities = availabilityRepository.findAvailabilitiesByDate(room.getId(), startDate, endDate);
        availabilityRepository.deleteAll(conflictedAvailabilities);

        for (Availability conflicted : conflictedAvailabilities) {
            if (conflicted.getStartDate().isBefore(startDate)) {
                availabilityRepository.save(new Availability(room, conflicted.getStartDate(), startDate.minusDays(1), conflicted.isAvailable()));
            }
            if (conflicted.getEndDate().isAfter(endDate)) {
                availabilityRepository.save(new Availability(room, endDate.plusDays(1), conflicted.getEndDate(), conflicted.isAvailable()));
            }
        }
    }

    /**
     * yearMonth에 해당하는 달의 price, availability 리스트 반환
     */
    public CalendarResponse getCalendar(long roomId, String currency, YearMonth yearMonth) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new InvalidRoomIdException(roomId));
        validateYearMonth(yearMonth);

        Currency requestCurrency = currencyService.getByCode(currency);
        Currency roomCurrency = room.getCurrency();

        List<Pricing> pricingList = pricingRepository.findPricingsByMonth(roomId, yearMonth);
        List<Availability> availabilities = availabilityRepository.findAvailabilitiesByMonth(roomId, yearMonth);

        Map<LocalDate, Integer> pricingMap = flattenPricingList(pricingList, yearMonth.atDay(1), yearMonth.atEndOfMonth());
        Map<LocalDate, Boolean> availabilityMap = flattenAvailabilities(availabilities, yearMonth.atDay(1), yearMonth.atEndOfMonth());

        List<DailyInfo> dailyInfos = new ArrayList<>();

        for (LocalDate date = yearMonth.atDay(1); !date.isAfter(yearMonth.atEndOfMonth()); date = date.plusDays(1)) {
            int amount = pricingMap.getOrDefault(date, room.getBasePrice());
            double price = currencyService.convert(roomCurrency, requestCurrency, amount);
            boolean isAvailable = availabilityMap.getOrDefault(date, false);
            dailyInfos.add(new DailyInfo(date, price, isAvailable));
        }

        return new CalendarResponse(roomId, currency, dailyInfos);
    }

    private Map<LocalDate, Integer> flattenPricingList(List<Pricing> pricingList, LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> pricingMap = new HashMap<>();

        for (Pricing pricing : pricingList) {
            for (LocalDate date = max(start, pricing.getStartDate()); !date.isAfter(min(end, pricing.getEndDate())); date = date.plusDays(1)) {
                pricingMap.put(date, pricing.getPricePerNight());
            }
        }

        return pricingMap;
    }

    private Map<LocalDate, Boolean> flattenAvailabilities(List<Availability> availabilities, LocalDate start, LocalDate end) {
        Map<LocalDate, Boolean> availabilityMap = new HashMap<>();

        for (Availability availability : availabilities) {
            for (LocalDate date = max(start, availability.getStartDate()); !date.isAfter(min(end, availability.getEndDate())); date = date.plusDays(1)) {
                availabilityMap.put(date, availability.isAvailable());
            }
        }

        return availabilityMap;
    }

    private LocalDate max(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2) ? date1 : date2;
    }

    private LocalDate min(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2) ? date1 : date2;
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

    private void validateYearMonth(YearMonth yearMonth) {
        if (yearMonth.isAfter(YearMonth.now().plusYears(1))) {
            throw new InvalidYearMonthException("1년 이내의 값만 조회 가능합니다.", yearMonth);
        }
    }

}
