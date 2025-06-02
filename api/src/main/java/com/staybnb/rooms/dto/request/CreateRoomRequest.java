package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.domain.vo.Address;
import com.staybnb.rooms.dto.CreateRoomCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

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
    private Set<String> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private String currency;

    public CreateRoomCommand toCommand() {
        return CreateRoomCommand.builder()
                .hostId(hostId)
                .placeType(placeType)
                .roomType(roomType)
                .address(address)
                .maxNumberOfGuests(maxNumberOfGuests)
                .bedrooms(bedrooms)
                .beds(beds)
                .amenities(amenities)
                .title(title)
                .description(description)
                .pricePerNight(pricePerNight)
                .currency(currency)
                .build();
    }
}
