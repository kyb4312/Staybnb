package com.staybnb.bookings.controller;

import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.dto.response.BookingResponse;
import com.staybnb.bookings.service.BookingService;
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

    @GetMapping("/listings/{roomId}")
    public PagedModel<BookingResponse> getBookings(@PathVariable Long roomId, Pageable pageable) {
        return new PagedModel<>(bookingService.findBookingsByRoomId(roomId, pageable).map(BookingResponse::fromEntity));
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse updateBooking(@PathVariable Long bookingId, @RequestBody String status) {
        return BookingResponse.fromEntity(bookingService.updateBooking(bookingId, BookingStatus.valueOf(status)));
    }
}
