package com.staybnb.bookings.controller;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.dto.request.CreateBookingRequest;
import com.staybnb.bookings.dto.request.GetBookingPreviewRequest;
import com.staybnb.bookings.dto.response.BookingPreviewResponse;
import com.staybnb.bookings.dto.response.BookingResponse;
import com.staybnb.bookings.service.BookingService;
import com.staybnb.common.auth.dto.LoginUser;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.service.RoomService;
import com.staybnb.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;
    private final Executor asyncExecutor;

    @GetMapping("/preview")
    public CompletableFuture<BookingPreviewResponse> getBookingPreview(@Valid @ModelAttribute GetBookingPreviewRequest request) {
//        log.info("step: controller entry → {}", Thread.currentThread().getName());
        return CompletableFuture
                .supplyAsync(() -> bookingService.getBookingPreview(request), asyncExecutor)
                .thenApply(BookingPreviewResponse::fromEntity);
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return CompletableFuture
                .supplyAsync(() -> bookingService.createBooking(toEntity(request)), asyncExecutor)
                .thenApply(booking -> {
                            URI location = UriComponentsBuilder
                                    .fromPath("/bookings/{bookingId}")
                                    .buildAndExpand(booking.getId())
                                    .toUri();

                            return ResponseEntity.created(location).body(BookingResponse.fromEntity(booking));
                        }
                );
    }

    @GetMapping("/{bookingId}")
    public CompletableFuture<BookingResponse> getBooking(@PathVariable Long bookingId, LoginUser loginUser) {
        return CompletableFuture
                .supplyAsync(() -> bookingService.getBooking(loginUser.getId(), bookingId), asyncExecutor)
                .thenApply(BookingResponse::fromEntity);
    }

    @DeleteMapping("/{bookingId}")
    public CompletableFuture<BookingResponse> cancelBooking(@PathVariable Long bookingId, LoginUser loginUser) {
        return CompletableFuture
                .supplyAsync(() -> bookingService.cancelBooking(loginUser.getId(), bookingId), asyncExecutor)
                .thenApply(BookingResponse::fromEntity);
    }

    @GetMapping("/upcoming")
    public CompletableFuture<PagedModel<BookingResponse>> findUpcomingBookings(Pageable pageable, LoginUser loginUser) {
        return CompletableFuture
                .supplyAsync(() -> bookingService.findUpcomingBookings(loginUser.getId(), pageable), asyncExecutor)
                .thenApply(pagedBooking -> new PagedModel<>(pagedBooking.map(BookingResponse::fromEntity)));
    }

    @GetMapping("/past")
    public CompletableFuture<PagedModel<BookingResponse>> findPastBookings(Pageable pageable, LoginUser loginUser) {
        return CompletableFuture
                .supplyAsync(() -> bookingService.findPastBookings(loginUser.getId(), pageable), asyncExecutor)
                .thenApply(pagedBooking -> new PagedModel<>(pagedBooking.map(BookingResponse::fromEntity)));
    }

    @GetMapping("/cancelled")
    public CompletableFuture<PagedModel<BookingResponse>> findCancelledBookings(Pageable pageable, LoginUser loginUser) {
        return CompletableFuture
                .supplyAsync(() -> bookingService.findCancelledBookings(loginUser.getId(), pageable), asyncExecutor)
                .thenApply(pagedBooking -> new PagedModel<>(pagedBooking.map(BookingResponse::fromEntity)));
    }

    private Booking toEntity(CreateBookingRequest request) {
        return new Booking(
                roomService.findById(request.getRoomId()),
                userService.findById(request.getGuestId()),
                request.getNumberOfGuests(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getBookingPrice(),
                Currency.valueOf(request.getCurrency())
        );
    }
}
