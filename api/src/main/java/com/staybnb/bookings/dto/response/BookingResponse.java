package com.staybnb.bookings.dto.response;

import com.staybnb.bookings.domain.Booking;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BookingResponse {

    private Long id;
    private Long roomId;
    private Long guestId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer numberOfGuests;
    private Double bookingPrice;
    private String currency;
    private String status;

    public BookingResponse(Long id, Long roomId, Long guestId, LocalDate checkIn, LocalDate checkOut, Integer numberOfGuests, Double bookingPrice, String currency, String status) {
        this.id = id;
        this.roomId = roomId;
        this.guestId = guestId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.bookingPrice = bookingPrice;
        this.currency = currency;
        this.status = status;
    }

    static public BookingResponse fromEntity(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getUser().getId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getNumberOfGuests(),
                booking.getBookingPrice(),
                booking.getCurrency().toString(),
                booking.getStatus().toString()
        );
    }
}
