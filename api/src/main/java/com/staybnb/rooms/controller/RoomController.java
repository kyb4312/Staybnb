package com.staybnb.rooms.controller;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.SearchRoomCondition;
import com.staybnb.rooms.dto.request.SearchPricingRequest;
import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.rooms.dto.response.CalendarResponse;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.dto.response.RoomResponse;
import com.staybnb.rooms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final PricingService pricingService;
    private final CalendarService pricingAndAvailabilityService;

    @GetMapping("/{roomId}")
    public RoomResponse getRoom(@PathVariable long roomId) {
        Room room = roomService.findById(roomId);
        return RoomResponse.fromDomain(room);
    }

    @GetMapping
    public Page<RoomResponse> getRooms(@Valid @ModelAttribute SearchRoomRequest searchRoomRequest, Pageable pageable) {
        return roomService.findAll(toCondition(searchRoomRequest), pageable)
                .map(RoomResponse::fromDomain);
    }

    @GetMapping("/{roomId}/pricing")
    public PricingResponse getTotalPricing(@PathVariable Long roomId, @Valid @ModelAttribute SearchPricingRequest searchPricingRequest) {
        return pricingService.getTotalPricing(roomId, searchPricingRequest);
    }

    @GetMapping("/{roomId}/calendar")
    public CalendarResponse getCalendar(@PathVariable long roomId, @RequestParam String currency, @RequestParam YearMonth yearMonth) {
        return pricingAndAvailabilityService.getCalendar(roomId, currency, yearMonth);
    }

    private SearchRoomCondition toCondition(SearchRoomRequest request) {
        return SearchRoomCondition.builder()
                .numberOfGuests(request.getGuests())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .priceFrom(request.getPriceFrom())
                .priceTo(request.getPriceTo())
                .currency(request.getCurrency())
                .build();
    }
}
