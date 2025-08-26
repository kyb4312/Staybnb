package com.staybnb.bookings.repository;

import com.staybnb.bookings.domain.TimezoneMidnight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface TimezoneMidnightRepository extends JpaRepository<TimezoneMidnight, String> {
    List<TimezoneMidnight> findByUtcMidnight(LocalTime utcMidnight);
}
