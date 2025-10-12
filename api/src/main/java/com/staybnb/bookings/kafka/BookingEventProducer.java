package com.staybnb.bookings.kafka;

import com.staybnb.common.event.BookingEvent;
import com.staybnb.bookings.domain.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventProducer {

    private static final String BOOKING_EVENTS_TOPIC = "booking-events";
    private final KafkaTemplate<Long, BookingEvent> kafkaTemplate;

    public void produceBookingEvent(Booking booking) {
        BookingEvent bookingEvent = new BookingEvent(
                booking.getId(),
                booking.getStatus(),
                booking.getRoom().getTitle(),
                booking.getRoom().getHost().getEmail(),
                booking.getRoom().getHost().getName(),
                booking.getUser().getEmail(),
                booking.getUser().getName(),
                booking.getNumberOfGuests(),
                booking.getCheckIn().toString(),
                booking.getCheckOut().toString(),
                booking.getBookingPrice()
        );

        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, booking.getId(), bookingEvent)
                .thenAccept(result -> {
                    log.info("Produced booking event: {}", result);
                })
                .exceptionally(ex -> {
                    log.error("Failed to produce booking event: {}", ex.getMessage());
                    return null;
                });
    }
}
