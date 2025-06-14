package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.dto.UpdateRoomCommand;
import com.staybnb.validation.annotation.NullOrNotBlank;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@Builder
@ToString
public class UpdateRoomRequest {

    @Min(1)
    private Integer maxNumberOfGuests;

    @Min(1)
    private Integer bedrooms;

    @Min(0)
    private Integer beds;

    private Set<String> amenities;

    @NullOrNotBlank
    private String title;

    @NullOrNotBlank
    private String description;

    @Min(0)
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
