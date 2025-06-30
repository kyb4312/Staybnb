package com.staybnb.bookings.controller;

import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.dto.response.BookingResponse;
import com.staybnb.bookings.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import static com.staybnb.common.constant.RequestAttributes.USER_ID;

@Slf4j
@RestController
@RequestMapping("/host/bookings")
@RequiredArgsConstructor
public class HostBookingController {

    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable Long bookingId, HttpServletRequest request) {
        return BookingResponse.fromEntity(bookingService.getBooking((Long) request.getAttribute(USER_ID), bookingId));
    }

    @GetMapping("/listings/{roomId}")
    public PagedModel<BookingResponse> getBookings(@PathVariable Long roomId, Pageable pageable, HttpServletRequest request) {
        return new PagedModel<>(bookingService.findBookingsByRoomId((Long) request.getAttribute(USER_ID), roomId, pageable).map(BookingResponse::fromEntity));
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse updateBooking(@PathVariable Long bookingId, @RequestBody String status, HttpServletRequest request) {
        return BookingResponse.fromEntity(bookingService.updateBooking((Long) request.getAttribute(USER_ID), bookingId, BookingStatus.valueOf(status)));
    }
}
