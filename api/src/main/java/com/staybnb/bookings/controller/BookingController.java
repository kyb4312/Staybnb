package com.staybnb.bookings.controller;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.dto.request.CreateBookingRequest;
import com.staybnb.bookings.dto.request.GetBookingPreviewRequest;
import com.staybnb.bookings.dto.response.BookingPreviewResponse;
import com.staybnb.bookings.dto.response.BookingResponse;
import com.staybnb.bookings.service.BookingService;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.service.RoomService;
import com.staybnb.users.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.staybnb.common.constant.RequestAttributes.USER_ID;

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
    public BookingResponse getBooking(@PathVariable Long bookingId, HttpServletRequest request) {
        return BookingResponse.fromEntity(bookingService.getBooking((Long) request.getAttribute(USER_ID), bookingId));
    }

    @DeleteMapping("/{bookingId}")
    public BookingResponse cancelBooking(@PathVariable Long bookingId, HttpServletRequest request) {
        return BookingResponse.fromEntity(bookingService.cancelBooking((Long) request.getAttribute(USER_ID), bookingId));
    }

    @GetMapping("/upcoming")
    public PagedModel<BookingResponse> findUpcomingBookings(Pageable pageable, HttpServletRequest request) {
        return new PagedModel<>(bookingService.findUpcomingBookings((Long) request.getAttribute(USER_ID), pageable).map(BookingResponse::fromEntity));
    }

    @GetMapping("/past")
    public PagedModel<BookingResponse> findPastBookings(Pageable pageable, HttpServletRequest request) {
        return new PagedModel<>(bookingService.findPastBookings((Long) request.getAttribute(USER_ID), pageable).map(BookingResponse::fromEntity));
    }

    @GetMapping("/cancelled")
    public PagedModel<BookingResponse> findCancelledBookings(Pageable pageable, HttpServletRequest request) {
        return new PagedModel<>(bookingService.findCancelledBookings((Long) request.getAttribute(USER_ID), pageable).map(BookingResponse::fromEntity));
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
