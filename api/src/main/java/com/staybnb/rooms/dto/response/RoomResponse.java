package com.staybnb.rooms.dto.response;

import com.staybnb.rooms.dto.Address;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class RoomResponse {
    private Long id;
    private Long hostId;
    private String placeType;
    private String roomType;
    private Address address;
    private Integer maxNumberOfGuests;
    private Integer bedrooms;
    private Integer beds;
    private List<String> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private String currency;
}
