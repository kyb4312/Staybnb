package com.staybnb.rooms.dto;

import com.staybnb.rooms.domain.vo.Amenity;
import com.staybnb.rooms.domain.vo.Currency;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class RoomUpdateInfo {
    private Integer maxNumberOfGuests;
    private Integer bedrooms;
    private Integer beds;
    private List<Amenity> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private Currency currency;
}
