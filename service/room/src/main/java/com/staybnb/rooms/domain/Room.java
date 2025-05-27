package com.staybnb.rooms.domain;

import com.staybnb.rooms.domain.vo.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class Room {
    @Setter
    private Long id;

    private Long hostId;
    private PlaceType placeType;
    private RoomType roomType;
    private Address address;
    private Integer maxNumberOfGuests;
    private Integer bedrooms;
    private Integer beds;
    private List<Amenity> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private Currency currency;

    private boolean isDeleted;
    private LocalDateTime deletedAt;

    public void delete(LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedAt = deletedAt;
    }
}
