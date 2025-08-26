package com.staybnb.bookings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingEventConsumer {

    private static final String BOOKING_EVENTS_TOPIC = "booking-events";
    private static final String NOTI_CONSUMER_GROUP = "notification-service";

    @KafkaListener(topics = BOOKING_EVENTS_TOPIC, groupId = NOTI_CONSUMER_GROUP)
    public void consumeBookingEvent(BookingEvent bookingEvent) {
        log.info("Received booking event: {} for booking ID: {}", bookingEvent.getEventType(), bookingEvent.getBookingId());
        // TODO: 알림 전송 로직 추가
    }
}
