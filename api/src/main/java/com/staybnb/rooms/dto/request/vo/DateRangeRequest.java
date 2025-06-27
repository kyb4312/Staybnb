package com.staybnb.rooms.dto.request.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

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
}
