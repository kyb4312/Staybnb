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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final PricingService pricingService;
    private final CalendarService pricingAndAvailabilityService;

    @GetMapping("/{roomId}")
    public CompletableFuture<RoomResponse> getRoom(@PathVariable long roomId) {
//        log.info("step: controller entry → {}", Thread.currentThread().getName());
        return roomService.getRoom(roomId).thenApply(RoomResponse::fromDomain);
    }

    @GetMapping
    public CompletableFuture<PagedModel<RoomResponse>> getRooms(@Valid @ModelAttribute SearchRoomRequest searchRoomRequest, Pageable pageable) {
        return roomService.findAll(toCondition(searchRoomRequest), pageable)
                .thenApply(rooms -> new PagedModel<>(rooms.map(RoomResponse::fromDomain)));
    }

    @GetMapping("/{roomId}/pricing")
    public CompletableFuture<PricingResponse> getTotalPricing(@PathVariable Long roomId, @Valid @ModelAttribute SearchPricingRequest searchPricingRequest) {
        return pricingService.getTotalPricing(roomId, searchPricingRequest);
    }

    @GetMapping("/{roomId}/calendar")
    public CompletableFuture<CalendarResponse> getCalendar(@PathVariable long roomId, @RequestParam String currency, @RequestParam YearMonth yearMonth) {
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
