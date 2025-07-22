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

@Slf4j
@Service
@RequiredArgsConstructor
public class TimezoneMidnightService {

    private final JobLauncher jobLauncher;
    private final Job timezoneUpdateJob;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateTimezoneMidnight() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLocalDate("date", LocalDate.now())
                    .toJobParameters();

            jobLauncher.run(timezoneUpdateJob, params);
        } catch (Exception e) {
            log.error("Exception 발생: {}", e.getMessage());
        }
    }
}
