package com.staybnb.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class RoomSearchCondition {
    Integer numberOfGuests;
    LocalDate startDate;
    LocalDate endDate;
    String location;
    Currency currency;
    Integer priceFrom;
    Integer priceTo;
}
