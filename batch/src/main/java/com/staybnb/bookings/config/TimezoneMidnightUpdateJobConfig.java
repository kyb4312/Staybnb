package com.staybnb.bookings.config;

import com.staybnb.bookings.domain.TimezoneMidnight;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.*;
import java.util.Map;

@Configuration
public class TimezoneMidnightUpdateJobConfig {

    @Bean
    public Job timezoneUpdateJob(JobRepository jobRepository, Step timezoneUpdateStep) {
        return new JobBuilder("timezoneUpdateJob", jobRepository)
                .start(timezoneUpdateStep)
                .build();
    }

    @Bean
    public Step timezoneUpdateStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<String> timezoneItemReader,
            ItemProcessor<String, TimezoneMidnight> timezoneItemProcessor,
            ItemWriter<TimezoneMidnight> timezoneItemWriter
    ) {
        return new StepBuilder("timezoneUpdateStep", jobRepository)
                .<String, TimezoneMidnight>chunk(100, transactionManager)
                .reader(timezoneItemReader)
                .processor(timezoneItemProcessor)
                .writer(timezoneItemWriter)
                .build();
    }

    @Bean
    public JdbcPagingItemReader<String> timezoneItemReader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<String>()
                .name("timezoneItemReader")
                .dataSource(dataSource)
                .selectClause("SELECT DISTINCT time_zone_id")
                .fromClause("FROM room")
                .sortKeys(Map.of("time_zone_id", Order.ASCENDING))
                .rowMapper(((rs, rowNum) -> rs.getString("time_zone_id")))
                .pageSize(100)
                .build();
    }

    @Bean
    ItemProcessor<String, TimezoneMidnight> timezoneItemProcessor() {
        return timeZoneId -> {
            ZoneId zoneId = ZoneId.of(timeZoneId);
            ZonedDateTime midnightInZone = ZonedDateTime.of(LocalDate.now(zoneId), LocalTime.MIDNIGHT, zoneId);
            ZonedDateTime utcTime = midnightInZone.withZoneSameInstant(ZoneOffset.UTC);
            return new TimezoneMidnight(timeZoneId, utcTime.toLocalTime());
        };
    }

    @Bean
    public JdbcBatchItemWriter<TimezoneMidnight> timezoneItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TimezoneMidnight>()
                .dataSource(dataSource)
                .sql("""
                            INSERT INTO timezone_midnight (time_zone_id, utc_midnight, updated_at)
                            VALUES (:timeZoneId, :utcMidnight, now())
                            ON CONFLICT (time_zone_id)
                            DO UPDATE SET utc_midnight = EXCLUDED.utc_midnight, updated_at = now()
                        """)
                .beanMapped()
                .build();
    }
}
