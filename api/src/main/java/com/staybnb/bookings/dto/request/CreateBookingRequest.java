package com.staybnb.bookings.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
public class CreateBookingRequest {

    @NotNull
    private Long roomId;

    @NotNull
    private Long userId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    @NotNull
    @Min(1)
    private Integer numberOfGuests;

    @NotNull
    private Double bookingPrice;

    @NotNull
    private String currency;

    public CreateBookingRequest(Long roomId, Long userId, LocalDate checkIn, LocalDate checkOut, Integer numberOfGuests, Double bookingPrice, String currency) {
        this.roomId = roomId;
        this.userId = userId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.bookingPrice = bookingPrice;
        this.currency = currency;
    }
}
