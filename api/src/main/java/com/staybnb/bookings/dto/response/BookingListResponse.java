package com.staybnb.bookings.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class BookingListResponse {

    private List<BookingResponse> bookings;

    public BookingListResponse(List<BookingResponse> bookings) {
        this.bookings = bookings;
    }
}
