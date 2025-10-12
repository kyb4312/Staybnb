package com.staybnb.common.event;

import com.staybnb.bookings.domain.vo.BookingStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private Long bookingId;
    private BookingStatus bookingStatus;
    private String roomTitle;
    private String hostEmail;
    private String hostName;
    private String guestEmail;
    private String guestName;
    private Integer numberOfGuests;
    private String checkIn;
    private String checkOut;
    private Double bookingPrice;
}
