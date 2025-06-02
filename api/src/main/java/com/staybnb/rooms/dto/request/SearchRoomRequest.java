package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.dto.SearchRoomCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class SearchRoomRequest {
    Integer guests;
    String startDate;
    String endDate;
    String location;
    Integer priceFrom;
    Integer priceTo;
    String currency;

    public SearchRoomCommand toCommand() {
        return SearchRoomCommand.builder()
                .numberOfGuests(guests)
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .location(location)
                .priceFrom(priceFrom)
                .priceTo(priceTo)
                .currency(currency)
                .build();
    }
}
