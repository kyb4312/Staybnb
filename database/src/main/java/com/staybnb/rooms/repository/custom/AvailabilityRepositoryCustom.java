package com.staybnb.rooms.repository.custom;

import com.staybnb.rooms.domain.Availability;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface AvailabilityRepositoryCustom {
    /**
     * [startDate, endDate] 구간과 겹치는 availability list 반환
     */
    List<Availability> findAvailabilitiesByDate(Long roomId, LocalDate startDate, LocalDate endDate);

    /**
     * yearMonth 달에 해당하는 availability list 반환
     */
    List<Availability> findAvailabilitiesByMonth(Long roomId, YearMonth yearMonth);
}
