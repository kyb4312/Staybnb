package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.domain.vo.Address;
import com.staybnb.rooms.dto.CreateRoomCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@Builder
@ToString
public class CreateRoomRequest {

    @NotNull
    private Long hostId;

    @NotBlank
    private String placeType;

    @NotBlank
    private String roomType;

    @NotNull
    private Address address;

    @NotNull
    @Min(1)
    private Integer maxNumberOfGuests;

    @NotNull
    @Min(1)
    private Integer bedrooms;

    @NotNull
    @Min(0)
    private Integer beds;

    private Set<String> amenities;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Min(0)
    private Integer pricePerNight;

    @NotBlank
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
