package com.staybnb.rooms.domain;

import com.staybnb.rooms.domain.vo.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
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

    public void updateMaxNumberOfGuests(Integer maxNumberOfGuests) {
        this.maxNumberOfGuests = maxNumberOfGuests;
    }

    public void updateBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public void updateBeds(Integer beds) {
        this.beds = beds;
    }

    public void updateAmenities(List<Amenity> amenities) {
        this.amenities = amenities;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePrice(Integer pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public void updateCurrency(Currency currency) {
        this.currency = currency;
    }

    public void delete(LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedAt = deletedAt;
    }
}
