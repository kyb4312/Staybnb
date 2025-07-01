package com.staybnb.bookings.dto.response;

import com.staybnb.bookings.domain.Booking;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BookingPreviewResponse {

    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer numberOfGuests;
    private Double bookingPrice;
    private String currency;

    public BookingPreviewResponse(Long roomId, LocalDate checkIn, LocalDate checkOut, Integer numberOfGuests, Double bookingPrice, String currency) {
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.bookingPrice = bookingPrice;
        this.currency = currency;
    }

    static public BookingPreviewResponse fromEntity(Booking booking) {
        return new BookingPreviewResponse(
                booking.getRoom().getId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getNumberOfGuests(),
                booking.getBookingPrice(),
                booking.getCurrency().toString()
        );
    }
}
