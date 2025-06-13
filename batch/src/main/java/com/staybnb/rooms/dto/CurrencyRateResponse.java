package com.staybnb.rooms.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
public class CurrencyRateResponse {
    String base;
    LocalDate date;
    Map<String, Double> rates;
}
