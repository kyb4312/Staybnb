package com.staybnb.bookings.dto.response;

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
    private Double basePrice;
    private Double servicePrice;
    private Double totalPrice;
    private String currency;
    private String status;

    public BookingResponse(Long id, Long roomId, Long guestId, LocalDate checkIn, LocalDate checkOut, Integer numberOfGuests, Double basePrice, Double servicePrice, Double totalPrice, String currency, String status) {
        this.id = id;
        this.roomId = roomId;
        this.guestId = guestId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.basePrice = basePrice;
        this.servicePrice = servicePrice;
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.status = status;
    }
}
