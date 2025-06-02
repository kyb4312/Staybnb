package com.staybnb.rooms.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class SearchRoomCommand {
    Integer numberOfGuests;
    LocalDate startDate;
    LocalDate endDate;
    String location;
    String currency;
    Integer priceFrom;
    Integer priceTo;
}
