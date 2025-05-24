package com.staybnb.rooms.controller;

import com.staybnb.domain.Address;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable long roomId) {
        RoomResponse roomResponse = getDummyRoomResponse(roomId);
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRooms(@ModelAttribute SearchRoomRequest searchRoomRequest) {
        List<RoomResponse> rooms = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {
            RoomResponse roomResponse = getDummyRoomResponse((long) i);
            rooms.add(roomResponse);
        }

        return ResponseEntity.ok(rooms);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody CreateRoomRequest createRoomRequest) {
        long roomId = 1L;
        RoomResponse roomResponse = getDummyRoomResponse(createRoomRequest, roomId);
        URI location = UriComponentsBuilder
                .fromPath("/rooms/{roomId}")
                .buildAndExpand(roomId)
                .toUri();

        return ResponseEntity.created(location).body(roomResponse);
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable long roomId, @RequestBody UpdateRoomRequest updateRoomRequest) {
        RoomResponse roomResponse = getDummyRoomResponse(updateRoomRequest, roomId);

        return ResponseEntity.ok(roomResponse);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable long roomId) {
        URI location = UriComponentsBuilder
                .fromPath("/users/{userId}/rooms")
                .buildAndExpand(roomId)
                .toUri();

        return ResponseEntity.noContent().location(location).build();
    }

    // dummy response data for api test
    private RoomResponse getDummyRoomResponse(long roomId) {
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        List<String> amenities = new ArrayList<>();
        amenities.add("wifi");
        amenities.add("air_conditioner");
        amenities.add("tv");
        amenities.add("kitchen");

        return RoomResponse.builder()
                .id(roomId)
                .hostId(1L)
                .placeType("building")
                .roomType("entirePlace")
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency("KRW")
                .build();
    }

    // dummy response data for api test
    private RoomResponse getDummyRoomResponse(CreateRoomRequest createRoomRequest, Long roomId) {
        return RoomResponse.builder()
                .id(roomId)
                .hostId(createRoomRequest.getHostId())
                .placeType(createRoomRequest.getPlaceType())
                .roomType(createRoomRequest.getRoomType())
                .address(createRoomRequest.getAddress())
                .maxNumberOfGuests(createRoomRequest.getMaxNumberOfGuests())
                .bedrooms(createRoomRequest.getBedrooms())
                .beds(createRoomRequest.getBeds())
                .amenities(createRoomRequest.getAmenities())
                .title(createRoomRequest.getTitle())
                .description(createRoomRequest.getDescription())
                .pricePerNight(createRoomRequest.getPricePerNight())
                .currency(createRoomRequest.getCurrency())
                .build();
    }

    // dummy response data for api test
    private RoomResponse getDummyRoomResponse(UpdateRoomRequest updateRoomRequest, Long roomId) {
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        return RoomResponse.builder()
                .id(roomId)
                .hostId(1L)
                .placeType("building")
                .roomType("entirePlace")
                .address(address)
                .maxNumberOfGuests(updateRoomRequest.getMaxNumberOfGuests())
                .bedrooms(updateRoomRequest.getBedrooms())
                .beds(updateRoomRequest.getBeds())
                .amenities(updateRoomRequest.getAmenities())
                .title(updateRoomRequest.getTitle())
                .description(updateRoomRequest.getDescription())
                .pricePerNight(updateRoomRequest.getPricePerNight())
                .currency(updateRoomRequest.getCurrency())
                .build();
    }
}
