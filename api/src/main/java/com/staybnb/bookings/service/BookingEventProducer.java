package com.staybnb.bookings.service;

import com.staybnb.bookings.BookingEvent;
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
        BookingEvent bookingEvent = new BookingEvent(booking.getStatus().toString(), booking.getId());

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
