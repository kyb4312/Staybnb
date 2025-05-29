package com.staybnb.rooms.domain;

import com.staybnb.rooms.domain.vo.*;
import com.staybnb.rooms.dto.RoomUpdateInfo;
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

    public void update(RoomUpdateInfo updateInfo) {
        if(updateInfo.getMaxNumberOfGuests() != null) {
            this.maxNumberOfGuests = updateInfo.getMaxNumberOfGuests();
        }
        if(updateInfo.getBedrooms() != null) {
            this.bedrooms = updateInfo.getBedrooms();
        }
        if(updateInfo.getBeds() != null) {
            this.beds = updateInfo.getBeds();
        }
        if (updateInfo.getAmenities() != null) {
            this.amenities = updateInfo.getAmenities();
        }
        if (updateInfo.getTitle() != null) {
            this.title = updateInfo.getTitle();
        }
        if (updateInfo.getDescription() != null) {
            this.description = updateInfo.getDescription();
        }
        if (updateInfo.getPricePerNight() != null) {
            this.pricePerNight = updateInfo.getPricePerNight();
        }
        if (updateInfo.getCurrency() != null) {
            this.currency = updateInfo.getCurrency();
        }
    }

    public void delete(LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedAt = deletedAt;
    }
}
