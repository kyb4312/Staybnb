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

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;

    @GetMapping("/preview")
    public BookingPreviewResponse getBookingPreview(@Valid @ModelAttribute GetBookingPreviewRequest request) {
        return BookingPreviewResponse.fromEntity(bookingService.getBookingPreview(request));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.createBooking(toEntity(request));

        URI location = UriComponentsBuilder
                .fromPath("/bookings/{bookingId}")
                .buildAndExpand(booking.getId())
                .toUri();

        return ResponseEntity.created(location).body(BookingResponse.fromEntity(booking));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable Long bookingId, LoginUser loginUser) {
        return BookingResponse.fromEntity(bookingService.getBooking(loginUser.getId(), bookingId));
    }

    @DeleteMapping("/{bookingId}")
    public BookingResponse cancelBooking(@PathVariable Long bookingId, LoginUser loginUser) {
        return BookingResponse.fromEntity(bookingService.cancelBooking(loginUser.getId(), bookingId));
    }

    @GetMapping("/upcoming")
    public PagedModel<BookingResponse> findUpcomingBookings(Pageable pageable, LoginUser loginUser) {
        return new PagedModel<>(bookingService.findUpcomingBookings(loginUser.getId(), pageable).map(BookingResponse::fromEntity));
    }

    @GetMapping("/past")
    public PagedModel<BookingResponse> findPastBookings(Pageable pageable, LoginUser loginUser) {
        return new PagedModel<>(bookingService.findPastBookings(loginUser.getId(), pageable).map(BookingResponse::fromEntity));
    }

    @GetMapping("/cancelled")
    public PagedModel<BookingResponse> findCancelledBookings(Pageable pageable, LoginUser loginUser) {
        return new PagedModel<>(bookingService.findCancelledBookings(loginUser.getId(), pageable).map(BookingResponse::fromEntity));
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
