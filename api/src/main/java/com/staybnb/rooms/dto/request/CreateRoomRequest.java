package com.staybnb.rooms.dto.request;

import com.staybnb.domain.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@ToString
public class CreateRoomRequest {
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

    public Room toDomain() {
        // TODO: Enum.valueOf 예외 처리 고민 필요
        List<Amenity> amenityList = new ArrayList<>();
        for (String amenity : amenities) {
            amenityList.add(Amenity.valueOf(amenity));
        }

        return Room.builder()
                .hostId(hostId)
                .placeType(PlaceType.valueOf(placeType))
                .roomType(RoomType.valueOf(roomType))
                .address(address)
                .maxNumberOfGuests(maxNumberOfGuests)
                .bedrooms(bedrooms)
                .beds(beds)
                .amenities(amenityList)
                .title(title)
                .description(description)
                .pricePerNight(pricePerNight)
                .currency(Currency.valueOf(currency))
                .build();
    }
}
