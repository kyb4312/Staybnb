package com.staybnb.bookings.config;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.TimezoneMidnight;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.repository.TimezoneMidnightRepository;
import io.hypersistence.utils.hibernate.type.range.Range;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class BookingStatusUpdateJobConfig {

    @Bean
    public Job bookingStatusUpdateJob(JobRepository jobRepository, Step bookingStatusUpdateStep) {
        return new JobBuilder("bookingStatusUpdateJob", jobRepository)
                .start(bookingStatusUpdateStep)
                .build();
    }

    @Bean
    public Step bookingStatusUpdateStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Booking> bookingItemReader,
            ItemProcessor<Booking, Booking> bookingItemProcessor,
            ItemWriter<Booking> bookingItemWriter
    ) {
        return new StepBuilder("bookingStatusUpdateStep", jobRepository)
                .<Booking, Booking>chunk(100, transactionManager)
                .reader(bookingItemReader)
                .processor(bookingItemProcessor)
                .writer(bookingItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Booking> bookingItemReader(
            DataSource dataSource,
            TimezoneMidnightRepository timezoneMidnightRepository,
            @Value("#{jobParameters['utcNow']}") LocalTime utcNow
    ) {

        List<String> targetTimezones = timezoneMidnightRepository
                .findByUtcMidnight(utcNow)
                .stream()
                .map(TimezoneMidnight::getTimeZoneId)
                .toList();

        return new JdbcPagingItemReaderBuilder<Booking>()
                .name("bookingItemReader")
                .dataSource(dataSource)
                .selectClause("SELECT *")
                .fromClause("FROM booking")
                .whereClause("time_zone_id IN (:timezones) AND status IN ('RESERVED', 'ONGOING')")
                .parameterValues(Map.of("timezones", targetTimezones))
                .sortKeys(Map.of("id", Order.ASCENDING))
                .rowMapper((rs, rowNum) -> {
                    Booking booking = new Booking();
                    booking.setId(rs.getLong("id"));
                    booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
                    booking.setDateRange(Range.localDateRange(rs.getString("date_range")));
                    booking.setTimeZoneId(rs.getString("time_zone_id"));
                    return booking;
                })
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<Booking, Booking> bookingItemProcessor() {
        return booking -> {
            LocalDate today = LocalDate.now(ZoneId.of(booking.getTimeZoneId()));
            if (booking.getStatus() == BookingStatus.RESERVED && today.isEqual(booking.getCheckIn())) {
                booking.setStatus(BookingStatus.ONGOING);
            } else if (today.isAfter(booking.getCheckOut())) {
                booking.setStatus(BookingStatus.ENDED);
            } else {
                return null;
            }
            return booking;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Booking> bookingItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Booking>()
                .dataSource(dataSource)
                .sql("""
                            UPDATE booking
                            SET status = ?, updated_at = now()
                            WHERE id = ?
                        """)
                .itemPreparedStatementSetter((booking, ps) -> {
                    ps.setString(1, booking.getStatus().toString());
                    ps.setLong(2, booking.getId());
                })
                .build();
    }
}
