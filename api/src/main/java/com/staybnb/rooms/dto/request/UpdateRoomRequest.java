package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.dto.UpdateRoomCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@Builder
@ToString
public class UpdateRoomRequest {
    private Integer maxNumberOfGuests;
    private Integer bedrooms;
    private Integer beds;
    private Set<String> amenities;
    private String title;
    private String description;
    private Integer pricePerNight;
    private String currency;

    public UpdateRoomCommand toCommand() {
        return UpdateRoomCommand.builder()
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
