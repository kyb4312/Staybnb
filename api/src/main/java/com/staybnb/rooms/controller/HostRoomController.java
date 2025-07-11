package com.staybnb.rooms.controller;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import com.staybnb.rooms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class HostRoomController {

    private final RoomService roomService;
    private final PricingService pricingService;
    private final AvailabilityService availabilityService;

    private final UserService userService;
    private final PlaceTypeService placeTypeService;
    private final AmenityService amenityService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
        Room room = roomService.save(toEntity(createRoomRequest));

        URI location = UriComponentsBuilder
                .fromPath("/rooms/{roomId}")
                .buildAndExpand(room.getId())
                .toUri();

        return ResponseEntity.created(location).body(RoomResponse.fromDomain(room));
    }

    @PatchMapping("/{roomId}")
    public RoomResponse updateRoom(@PathVariable long roomId, @Valid @RequestBody UpdateRoomRequest updateRoomRequest) {
        Room room = roomService.update(roomId, updateRoomRequest);

        return RoomResponse.fromDomain(room);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable long roomId) {
        roomService.delete(roomId);
    }

    @PostMapping("/{roomId}/pricing")
    public void updatePricing(@PathVariable long roomId, @Valid @RequestBody UpdatePricingRequest updatePricingRequest) {
        pricingService.updateSelectedDatesPricing(roomId, updatePricingRequest);
    }

    @PostMapping("/{roomId}/availability")
    public void updateAvailability(@PathVariable long roomId, @Valid @RequestBody UpdateAvailabilityRequest updateAvailabilityRequest) {
        availabilityService.updateSelectedDatesAvailability(roomId, updateAvailabilityRequest);
    }


    private Room toEntity(CreateRoomRequest request) {
        return Room.builder()
                .host(userService.findById(request.getHostId()))
                .placeType(placeTypeService.getByName(request.getPlaceType()))
                .roomType(RoomType.valueOf(request.getRoomType()))
                .address(request.getAddress())
                .maxNumberOfGuests(request.getMaxNumberOfGuests())
                .bedrooms(request.getBedrooms())
                .beds(request.getBeds())
                .amenities(amenityService.getAmenitySetByStringSet(request.getAmenities()))
                .title(request.getTitle())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .currency(Currency.valueOf(request.getCurrency()))
                .build();
    }
}
