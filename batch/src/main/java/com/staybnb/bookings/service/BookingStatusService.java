package com.staybnb.bookings.service;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingStatusService {

    private final BookingRepository bookingRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateBookingStatus() {
        log.info("Updating Booking Status");

        int page = 0;
        int size = 100;
        int updatedCount = 0;
        Page<Booking> bookingPage;

        do {
            bookingPage = bookingRepository.findBookingsByStatus(PageRequest.of(page, size), BookingStatus.RESERVED.toString());
            for (Booking booking : bookingPage.getContent()) {
                if (booking.getCheckOut().isBefore(LocalDate.now())) {
                    booking.setStatus(BookingStatus.ENDED);
                    updatedCount++;
                    log.debug("updated booking: {}", booking.getCheckOut());
                }
            }
            page++;
        } while (!bookingPage.isLast());

        log.info("Booking Status Updated. updatedRows: {}", updatedCount);
    }
}
