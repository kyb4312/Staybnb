package com.staybnb.bookings.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
public class GetBookingPreviewRequest {

    @NotNull
    private Long roomId;

    @NotNull
    @Min(1)
    private Integer numberOfGuests;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    @NotNull
    private String guestCurrency;

    public GetBookingPreviewRequest(Long roomId, Integer numberOfGuests, LocalDate checkIn, LocalDate checkOut, String guestCurrency) {
        this.roomId = roomId;
        this.numberOfGuests = numberOfGuests;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestCurrency = guestCurrency;
    }
}
