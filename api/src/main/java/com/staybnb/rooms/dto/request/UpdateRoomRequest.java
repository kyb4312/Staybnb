package com.staybnb.rooms.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class UpdateRoomRequest {
    private Integer maxNumberOfGuests;
    private Integer bedrooms;
    private Integer beds;
    private List<String> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private String currency;
}
