package com.staybnb.bookings.config;

import com.staybnb.AbstractBatchTest;
import com.staybnb.bookings.domain.TimezoneMidnight;
import com.staybnb.bookings.repository.TimezoneMidnightRepository;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimezoneMidnightUpdateJobConfigTest extends AbstractBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job timezoneUpdateJob;

    @Autowired
    private TimezoneMidnightRepository timezoneMidnightRepository;

    @PostConstruct
    public void configureJobLauncherTestUtils() {
        jobLauncherTestUtils.setJob(timezoneUpdateJob);
    }

    @Test
    void timezoneUpdateJobTest() throws Exception {
        JobParameters params = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addLocalDate("date", LocalDate.now())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(params);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        long writeCount = stepExecution.getWriteCount();

        assertThat(writeCount).isEqualTo(2L);

        List<TimezoneMidnight> timezoneMidnights = timezoneMidnightRepository.findAll();
        timezoneMidnights.forEach(timezoneMidnight -> {
            assertThat(timezoneMidnight.getUpdatedAt())
                    .isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusSeconds(5));
        });
    }
}