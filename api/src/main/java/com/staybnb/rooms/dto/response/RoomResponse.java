package com.staybnb.rooms.dto.response;

import com.staybnb.rooms.domain.vo.Address;
import com.staybnb.rooms.domain.vo.Amenity;
import com.staybnb.rooms.domain.Room;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
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

    public static RoomResponse fromDomain(Room room) {
        List<String> amenityList = new ArrayList<>();
        for(Amenity amenity : room.getAmenities()) {
            amenityList.add(amenity.toString());
        }

        return RoomResponse.builder()
                .id(room.getId())
                .hostId(room.getHostId())
                .placeType(room.getPlaceType().toString())
                .roomType(room.getRoomType().toString())
                .address(room.getAddress())
                .maxNumberOfGuests(room.getMaxNumberOfGuests())
                .bedrooms(room.getBedrooms())
                .beds(room.getBeds())
                .amenities(amenityList)
                .title(room.getTitle())
                .description(room.getDescription())
                .pricePerNight(room.getPricePerNight())
                .currency(room.getCurrency().toString())
                .build();
    }
}
