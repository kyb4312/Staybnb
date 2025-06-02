package com.staybnb.rooms.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@Builder
@ToString
public class UpdateRoomCommand {
    private Integer maxNumberOfGuests;
    private Integer bedrooms;
    private Integer beds;
    private Set<String> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private String currency;
}
