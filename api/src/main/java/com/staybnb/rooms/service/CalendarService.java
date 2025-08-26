package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.Availability;
import com.staybnb.rooms.domain.Pricing;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.response.CalendarResponse;
import com.staybnb.rooms.dto.response.vo.DailyInfo;
import com.staybnb.common.exception.custom.InvalidYearMonthException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final PricingService pricingService;
    private final AvailabilityService availabilityService;
    private final RoomService roomService;
    private final ExchangeRateService exchangeRateService;

    /**
     * yearMonth에 해당하는 달의 price, availability 리스트 반환
     */
    @Async
    public CompletableFuture<CalendarResponse> getCalendar(long roomId, String currency, YearMonth yearMonth) {
        Room room = roomService.findById(roomId);
        validateYearMonth(yearMonth);

        List<Pricing> pricingList = pricingService.findPricingsByMonth(roomId, yearMonth);
        List<Availability> availabilities = availabilityService.findAvailabilitiesByMonth(roomId, yearMonth);

        Map<LocalDate, Integer> pricingMap = flattenPricingList(pricingList, yearMonth);
        Map<LocalDate, Boolean> availabilityMap = flattenAvailabilities(availabilities, yearMonth);

        List<DailyInfo> dailyInfos = new ArrayList<>();

        for (LocalDate date = yearMonth.atDay(1); !date.isAfter(yearMonth.atEndOfMonth()); date = date.plusDays(1)) {
            int amount = pricingMap.getOrDefault(date, room.getBasePrice());
            double price = exchangeRateService.convert(room.getCurrency(), Currency.valueOf(currency), amount);
            boolean isAvailable = availabilityMap.getOrDefault(date, false);
            dailyInfos.add(new DailyInfo(date, price, isAvailable));
        }

        return CompletableFuture.completedFuture(new CalendarResponse(roomId, currency, dailyInfos));
    }

    private Map<LocalDate, Integer> flattenPricingList(List<Pricing> pricingList, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.plusMonths(1).atDay(1);
        Map<LocalDate, Integer> pricingMap = new HashMap<>();

        for (Pricing pricing : pricingList) {
            for (LocalDate date = max(start, pricing.getStartDate()); date.isBefore(min(end, pricing.getEndDate())); date = date.plusDays(1)) {
                pricingMap.put(date, pricing.getPricePerNight());
            }
        }

        return pricingMap;
    }

    private Map<LocalDate, Boolean> flattenAvailabilities(List<Availability> availabilities, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.plusMonths(1).atDay(1);
        Map<LocalDate, Boolean> availabilityMap = new HashMap<>();

        for (Availability availability : availabilities) {
            for (LocalDate date = max(start, availability.getStartDate()); date.isBefore(min(end, availability.getEndDate())); date = date.plusDays(1)) {
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

    private void validateYearMonth(YearMonth yearMonth) {
        if (yearMonth.isAfter(YearMonth.now().plusYears(1))) {
            throw new InvalidYearMonthException("1년 이내의 값만 조회 가능합니다.", yearMonth);
        }
    }

}
