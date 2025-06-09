package com.staybnb.rooms.dto;

import com.staybnb.rooms.domain.vo.Address;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@Builder
@ToString
public class CreateRoomCommand {
    private Long hostId;
    private String placeType;
    private String roomType;
    private Address address;
    private Integer maxNumberOfGuests;
    private Integer bedrooms;
    private Integer beds;
    private Set<String> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private String currency;
}
