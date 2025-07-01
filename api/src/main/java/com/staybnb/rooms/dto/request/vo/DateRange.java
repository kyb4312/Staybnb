package com.staybnb.rooms.dto.request.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DateRange {
    private LocalDate startDate; // inclusive
    private LocalDate endDate; // exclusive
}
