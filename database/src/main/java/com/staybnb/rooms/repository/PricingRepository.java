package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PricingRepository extends JpaRepository<Pricing, Long> {

    @NativeQuery(value = """
            SELECT * FROM pricing
            WHERE room_id = :roomId
                AND date_range && daterange(:startDate, :endDate, '[)')
            """)
    List<Pricing> findPricingsByDate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDateInclusive,
            @Param("endDate") LocalDate endDateExclusive
    );
}
