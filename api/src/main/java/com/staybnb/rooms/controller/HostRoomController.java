package com.staybnb.rooms.controller;

import com.staybnb.common.auth.dto.LoginUser;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import com.staybnb.rooms.service.*;
import com.staybnb.users.service.UserService;
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
@RequestMapping("/host/rooms")
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
    public RoomResponse updateRoom(@PathVariable long roomId,
                                   @Valid @RequestBody UpdateRoomRequest updateRoomRequest,
                                   LoginUser loginUser) {

        Room room = roomService.update(loginUser.getId(), roomId, updateRoomRequest);
        return RoomResponse.fromDomain(room);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable long roomId, LoginUser loginUser) {
        roomService.delete(loginUser.getId(), roomId);
    }

    @PostMapping("/{roomId}/pricing")
    public void updatePricing(@PathVariable long roomId,
                              @Valid @RequestBody UpdatePricingRequest updatePricingRequest,
                              LoginUser loginUser) {

        pricingService.updateSelectedDatesPricing(loginUser.getId(), roomId, updatePricingRequest);
    }

    @PostMapping("/{roomId}/availability")
    public void updateAvailability(@PathVariable long roomId,
                                   @Valid @RequestBody UpdateAvailabilityRequest updateAvailabilityRequest,
                                   LoginUser loginUser) {

        availabilityService.updateSelectedDatesAvailability(
                loginUser.getId(), roomId, updateAvailabilityRequest);
    }

    @PostMapping("/{roomId}/availability/sql")
    public void updateAvailabilitySql(@PathVariable long roomId,
                                      @Valid @RequestBody UpdateAvailabilityRequest updateAvailabilityRequest,
                                      LoginUser loginUser) {

        availabilityService.updateSelectedDatesAvailabilitySql(
                loginUser.getId(), roomId, updateAvailabilityRequest);
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
                .timeZoneId(request.getTimeZoneId())
                .build();
    }
}
