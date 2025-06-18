package com.staybnb.rooms.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.staybnb.rooms.domain.Pricing;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.staybnb.rooms.domain.QPricing.pricing;

@Repository
public class PricingRepositoryCustomImpl implements PricingRepositoryCustom {

    private final JPAQueryFactory query;

    PricingRepositoryCustomImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public List<Pricing> findPricingsByDate(Long roomId, LocalDate startDate, LocalDate endDate) {
        return query
                .select(pricing)
                .from(pricing)
                .where(
                        roomId(roomId),
                        startBefore(endDate),
                        endAfter(startDate)
                )
                .fetch();
    }

    @Override
    public List<Pricing> findPricingsByMonth(Long roomId, YearMonth yearMonth) {
        return query
                .select(pricing)
                .from(pricing)
                .where(
                        roomId(roomId),
                        startBefore(yearMonth.atEndOfMonth()),
                        endAfter(yearMonth.atDay(1))
                )
                .orderBy(pricing.startDate.asc())
                .fetch();
    }

    private BooleanExpression roomId(Long id) {
        return pricing.room.id.eq(id);
    }

    private BooleanExpression startBefore(LocalDate endDate) {
        return pricing.startDate.loe(endDate);
    }

    private BooleanExpression endAfter(LocalDate startDate) {
        return pricing.endDate.goe(startDate);
    }
}
