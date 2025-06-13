package com.staybnb.rooms.repository.custom;

import com.staybnb.rooms.domain.Pricing;

import java.time.LocalDate;
import java.util.List;

public interface PricingRepositoryCustom {
    /**
     * [startDate, endDate] 구간과 겹치는 pricing list 반환
     */
    List<Pricing> findPricingsByDate(Long roomId, LocalDate startDate, LocalDate endDate);
}
