package com.staybnb.notification.kafka;

import com.staybnb.common.event.BookingEvent;
import com.staybnb.notification.dto.MailTarget;
import com.staybnb.notification.service.BookingMailTemplateResolver;
import com.staybnb.notification.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventConsumer {

    private static final String BOOKING_EVENTS_TOPIC = "booking-events";
    private static final String NOTI_CONSUMER_GROUP = "notification-service";

    private final BookingMailTemplateResolver bookingMailTemplateResolver;
    private final MailService emailService;

    @KafkaListener(topics = BOOKING_EVENTS_TOPIC, groupId = NOTI_CONSUMER_GROUP)
    public void consumeBookingEvent(BookingEvent bookingEvent) {
        log.info("Received booking event: {} for booking ID: {}", bookingEvent.getBookingStatus(), bookingEvent.getBookingId());

        List<MailTarget> mailTargets = bookingMailTemplateResolver.resolve(bookingEvent);

        for (MailTarget mailTarget : mailTargets) {
            emailService.sendEmail(mailTarget.email(), mailTarget.subject(), mailTarget.body());
        }
    }
}
