package com.staybnb.rooms.controller;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.request.*;
import com.staybnb.rooms.dto.response.CalendarResponse;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.dto.response.RoomResponse;
import com.staybnb.rooms.service.PricingAndAvailabilityService;
import com.staybnb.rooms.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.YearMonth;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final PricingAndAvailabilityService pricingAndAvailabilityService;

    @GetMapping("/{roomId}")
    public RoomResponse getRoom(@PathVariable long roomId) {
        Room room = roomService.findById(roomId);
        return RoomResponse.fromDomain(room);
    }

    @GetMapping
    public Page<RoomResponse> getRooms(@Valid @ModelAttribute SearchRoomRequest searchRoomRequest, Pageable pageable) {
        return roomService.findAll(searchRoomRequest, pageable)
                .map(RoomResponse::fromDomain);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
        Room room = roomService.save(createRoomRequest);
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
        pricingAndAvailabilityService.updateSelectedDatesPricing(roomId, updatePricingRequest);
    }

    @GetMapping("/{roomId}/pricing")
    public PricingResponse getPricing(@PathVariable Long roomId, @Valid @ModelAttribute SearchPricingRequest searchPricingRequest) {
        return pricingAndAvailabilityService.getPricing(roomId, searchPricingRequest);
    }

    @PostMapping("/{roomId}/availability")
    public void updateAvailability(@PathVariable long roomId, @Valid @RequestBody UpdateAvailabilityRequest updateAvailabilityRequest) {
        pricingAndAvailabilityService.updateSelectedDatesAvailability(roomId, updateAvailabilityRequest);
    }

    @GetMapping("/{roomId}/calendar")
    public CalendarResponse getCalendar(@PathVariable long roomId, @RequestParam String currency, @RequestParam YearMonth yearMonth) {
        return pricingAndAvailabilityService.getCalendar(roomId, currency, yearMonth);
    }
}
