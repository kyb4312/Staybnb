package com.staybnb.rooms.dto.request;

import com.staybnb.domain.Amenity;
import com.staybnb.domain.Currency;
import com.staybnb.domain.RoomUpdateInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@ToString
public class UpdateRoomRequest {
    private Integer maxNumberOfGuests;
    private Integer bedrooms;
    private Integer beds;
    private List<String> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private String currency;

    public RoomUpdateInfo toDomain() {
        // TODO: Enum.valueOf 예외 처리 고민 필요
        List<Amenity> amenityList = new ArrayList<>();
        if(amenities != null) {
            for (String amenity : amenities) {
                amenityList.add(Amenity.valueOf(amenity));
            }
        }

        return RoomUpdateInfo.builder()
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
