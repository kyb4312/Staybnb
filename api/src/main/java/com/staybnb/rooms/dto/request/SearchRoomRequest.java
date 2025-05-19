package com.staybnb.rooms.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

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
}
