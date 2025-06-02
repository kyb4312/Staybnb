package com.staybnb.rooms.dto.response;

import com.staybnb.rooms.domain.vo.Address;
import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.domain.Room;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

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
    private Set<String> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private String currency;

    public static RoomResponse fromDomain(Room room) {
        Set<String> amenitySet = new HashSet<>();
        for(Amenity amenity : room.getAmenities()) {
            amenitySet.add(amenity.getName());
        }

        return RoomResponse.builder()
                .id(room.getId())
                .hostId(room.getHost().getId())
                .placeType(room.getPlaceType().getName())
                .roomType(room.getRoomType().toString())
                .address(room.getAddress())
                .maxNumberOfGuests(room.getMaxNumberOfGuests())
                .bedrooms(room.getBedrooms())
                .beds(room.getBeds())
                .amenities(amenitySet)
                .title(room.getTitle())
                .description(room.getDescription())
                .pricePerNight(room.getPricePerNight())
                .currency(room.getCurrency().getCode())
                .build();
    }
}
