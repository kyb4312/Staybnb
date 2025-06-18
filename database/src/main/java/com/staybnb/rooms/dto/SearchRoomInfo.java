package com.staybnb.rooms.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class SearchRoomInfo {

    private Integer numberOfGuests;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String currency;
    private Integer priceFrom;
    private Integer priceTo;

}
