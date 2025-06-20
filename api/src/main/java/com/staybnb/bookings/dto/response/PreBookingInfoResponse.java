package com.staybnb.bookings.dto.response;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PreBookingInfoResponse {

    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer numberOfGuests;
    private Double basePrice;
    private Double serviceFee;
    private Double totalPrice;
    private String currency;
    
    public PreBookingInfoResponse(Long roomId, LocalDate checkIn, LocalDate checkOut, Integer numberOfGuests, Double basePrice, Double serviceFee, Double totalPrice, String currency) {
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.basePrice = basePrice;
        this.serviceFee = serviceFee;
        this.totalPrice = totalPrice;
        this.currency = currency;
    }
}
