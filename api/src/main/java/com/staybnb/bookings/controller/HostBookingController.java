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

@Slf4j
@RestController
@RequestMapping("/host/bookings")
@RequiredArgsConstructor
public class HostBookingController {

    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable Long bookingId, LoginUser loginUser) {
        return BookingResponse.fromEntity(bookingService.getBooking(loginUser.getId(), bookingId));
    }

    @GetMapping("/listings/{roomId}")
    public PagedModel<BookingResponse> getBookings(@PathVariable Long roomId, Pageable pageable, LoginUser loginUser) {
        return new PagedModel<>(bookingService.findBookingsByRoomId(loginUser.getId(), roomId, pageable).map(BookingResponse::fromEntity));
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse updateBooking(@PathVariable Long bookingId, @RequestBody String status, LoginUser loginUser) {
        return BookingResponse.fromEntity(bookingService.updateBooking(loginUser.getId(), bookingId, BookingStatus.valueOf(status)));
    }
}
