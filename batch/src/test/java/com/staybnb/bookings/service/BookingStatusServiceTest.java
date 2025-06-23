package com.staybnb.bookings.service;

import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class BookingStatusServiceTest {

    @Autowired
    private BookingStatusService bookingStatusService;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void updateBookingStatus() {
        bookingStatusService.updateBookingStatus();

        bookingRepository.findAll().forEach(booking -> {
            if (booking.getCheckOut().isBefore(LocalDate.now())) {
                Assertions.assertThat(booking.getStatus()).isNotEqualTo(BookingStatus.RESERVED);
            }
        });
    }
}