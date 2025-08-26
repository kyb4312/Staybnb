package com.staybnb.bookings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingStatusService {

    private final JobLauncher jobLauncher;
    private final Job bookingStatusUpdateJob;

    @Scheduled(cron = "0 */15 * * * ?")
    public void updateBookingStatus() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLocalDate("date", LocalDate.now())
                    .addLocalTime("utcNow", LocalTime.now(ZoneOffset.UTC))
                    .toJobParameters();

            jobLauncher.run(bookingStatusUpdateJob, params);
        } catch (Exception e) {
            log.error("Exception 발생: {}", e.getMessage());
        }
    }
}
