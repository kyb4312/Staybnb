package com.staybnb.rooms.dto.request.vo;

import com.staybnb.common.exception.custom.InvalidDateRangeException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class DateRangeRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate; // inclusive

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate; // inclusive

    public DateRange toDateRange() {
        return new DateRange(startDate, endDate.plusDays(1));
    }

    public static void sortAndValidateDateSelected(List<DateRangeRequest> dateSelected) {
        dateSelected = sortDateSelected(dateSelected);
        validateDateSelected(dateSelected);
    }

    private static List<DateRangeRequest> sortDateSelected(List<DateRangeRequest> dateSelected) {
        return dateSelected.stream()
                .sorted(Comparator.comparing(DateRangeRequest::getStartDate))
                .collect(Collectors.toList());
    }

    private static void validateDateSelected(List<DateRangeRequest> dateSelected) {
        dateSelected.forEach(dateRange -> {
            if (dateRange.getStartDate().isAfter(dateRange.getEndDate())) {
                throw new InvalidDateRangeException("startDate는 endDate 보다 같거나 이전 일자여야 합니다.",
                        dateRange.getStartDate(), dateRange.getEndDate());
            }
        });

        if (dateSelected.getFirst().getStartDate().isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException("startDate가 과거 일자입니다.",
                    dateSelected.getFirst().getStartDate(), dateSelected.getFirst().getEndDate());
        }

        if (!dateSelected.getLast().getEndDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new InvalidDateRangeException("1년 이내의 가격만 설정 가능합니다.",
                    dateSelected.getLast().getStartDate(), dateSelected.getLast().getEndDate());
        }

        for (int i = 0; i < dateSelected.size() - 1; i++) {
            if (!dateSelected.get(i).getEndDate().isBefore(dateSelected.get(i + 1).getStartDate())) {
                throw new InvalidDateRangeException("날짜 범위가 겹칩니다.",
                        dateSelected.get(i).getStartDate(), dateSelected.get(i).getEndDate(),
                        dateSelected.get(i + 1).getStartDate(), dateSelected.get(i + 1).getEndDate());
            }
        }
    }
}
