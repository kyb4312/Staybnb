package com.staybnb.bookings;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private String eventType;
    private Long bookingId;
}
