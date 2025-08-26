package com.staybnb.bookings.controller;

import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.dto.response.BookingResponse;
import com.staybnb.bookings.service.BookingService;
import com.staybnb.common.auth.dto.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/host/bookings")
@RequiredArgsConstructor
public class HostBookingController {

    private final BookingService bookingService;
    private final Executor asyncExecutor;

    @GetMapping("/{bookingId}")
    public CompletableFuture<BookingResponse> getBooking(@PathVariable Long bookingId, LoginUser loginUser) {
        return CompletableFuture
                .supplyAsync(() -> bookingService.getBooking(loginUser.getId(), bookingId), asyncExecutor)
                .thenApply(BookingResponse::fromEntity);
    }

    @GetMapping("/listings/{roomId}")
    public CompletableFuture<PagedModel<BookingResponse>> getBookings(@PathVariable Long roomId, Pageable pageable, LoginUser loginUser) {
        return CompletableFuture
                .supplyAsync(() -> bookingService.findBookingsByRoomId(loginUser.getId(), roomId, pageable), asyncExecutor)
                .thenApply(pagedBooking -> new PagedModel<>(pagedBooking.map(BookingResponse::fromEntity)));
    }

    @PatchMapping("/{bookingId}")
    public CompletableFuture<BookingResponse> updateBooking(@PathVariable Long bookingId, @RequestBody String status, LoginUser loginUser) {
        log.info("step: controller entry → {}", Thread.currentThread().getName());
        return CompletableFuture
                .supplyAsync(() -> bookingService.updateBooking(loginUser.getId(), bookingId, BookingStatus.valueOf(status)), asyncExecutor)
                .thenApply(BookingResponse::fromEntity);
    }
}
