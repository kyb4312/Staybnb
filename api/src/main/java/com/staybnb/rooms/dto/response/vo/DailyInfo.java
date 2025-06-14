package com.staybnb.rooms.dto.response.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyInfo {

    private LocalDate date;
    private Double price;
    private Boolean isAvailable;
}
