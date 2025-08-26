package com.staybnb.bookings.config;

import com.staybnb.AbstractBatchTest;
import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.repository.BookingRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class BookingStatusUpdateJobConfigTest extends AbstractBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job bookingStatusUpdateJob;

    @Autowired
    private BookingRepository bookingRepository;

    @PostConstruct
    public void configureJobLauncherTestUtils() {
        jobLauncherTestUtils.setJob(bookingStatusUpdateJob);
    }

    @Test
    void bookingStatusUpdateJobTest() throws Exception {
        JobParameters params = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addLocalDate("date", LocalDate.now())
                .addLocalTime("utcNow", LocalTime.parse("15:00:00"))
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(params);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        long writeCount = stepExecution.getWriteCount();

        assertThat(writeCount).isEqualTo(2L);

        Booking booking6 = bookingRepository.findById(6L).get(); // Seoul, daterange(CURRENT_DATE - 2, current_DATE - 1)
        Booking booking7 = bookingRepository.findById(7L).get(); // Seoul, daterange(CURRENT_DATE, current_DATE + 2)
        Booking booking8 = bookingRepository.findById(8L).get(); // New York, daterange(CURRENT_DATE - 2, current_DATE - 1)
        Booking booking9 = bookingRepository.findById(9L).get(); // New York, daterange(CURRENT_DATE, current_DATE + 2)

        assertThat(booking6.getStatus()).isEqualTo(BookingStatus.ENDED);
        assertThat(booking7.getStatus()).isEqualTo(BookingStatus.ONGOING);
        assertThat(booking8.getStatus()).isEqualTo(BookingStatus.ONGOING);
        assertThat(booking9.getStatus()).isEqualTo(BookingStatus.RESERVED);
    }
}