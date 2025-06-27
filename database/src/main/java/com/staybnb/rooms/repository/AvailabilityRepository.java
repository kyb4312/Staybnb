package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    /**
     * room_id가 같고,
     * date_range가 [startDateInclusive, endDateExclusive) 구간과 겹치는 availability 조회
     */
    @NativeQuery(value = """
            SELECT * FROM availability
            WHERE room_id = :roomId
                AND date_range && daterange(:startDate, :endDate, '[)')
            """)
    List<Availability> findAvailabilitiesByDate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDateInclusive,
            @Param("endDate") LocalDate endDateExclusive
    );

    /**
     * room_id가 같고,
     * date_range가 [startDateInclusive, endDateExclusive) 구간과 겹치는 availability 를
     * date_range 오름차순으로 조회
     */
    @NativeQuery(value = """
            SELECT * FROM availability
            WHERE room_id = :roomId
                AND date_range && daterange(:startDate, :endDate, '[)')
            ORDER BY date_range
            """)
    List<Availability> findOrderedAvailabilitiesByDate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDateInclusive,
            @Param("endDate") LocalDate endDateExclusive
    );

    /**
     * is_available이 true 이고,
     * room_id가 같고,
     * date_range가 [startDateInclusive, endDateExclusive) 구간과 겹치는 availability 조회
     */
    @NativeQuery(value = """
            SELECT * FROM availability
            WHERE is_available = true
                AND room_id = :roomId
                AND date_range && daterange(:startDate, :endDate, '[)')
            ORDER BY date_range
            """)
    List<Availability> findTrueAvailabilitiesByDate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDateInclusive,
            @Param("endDate") LocalDate endDateExclusive
    );

    /**
     * is_available이 true 이고,
     * room_id가 같고,
     * date_range가 [startDateInclusive, endDateExclusive) 구간과 겹치는 availability 를
     * Row-level 락을 가지고 조회
     */
    @NativeQuery(value = """
            SELECT * FROM availability
            WHERE is_available = true
                AND room_id = :roomId
                AND date_range && daterange(:startDate, :endDate, '[)')
            ORDER BY date_range
            FOR UPDATE
            """)
    List<Availability> findTrueAvailabilitiesByDateForUpdate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDateInclusive,
            @Param("endDate") LocalDate endDateExclusive
    );

}
