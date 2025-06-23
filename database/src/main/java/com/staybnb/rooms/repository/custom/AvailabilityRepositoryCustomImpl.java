package com.staybnb.rooms.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.staybnb.rooms.domain.Availability;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.staybnb.rooms.domain.QAvailability.availability;

@Repository
public class AvailabilityRepositoryCustomImpl implements AvailabilityRepositoryCustom {

    private final JPAQueryFactory query;

    AvailabilityRepositoryCustomImpl(EntityManager em) {
        query = new JPAQueryFactory(em);
    }

    @Override
    public List<Availability> findAvailabilitiesByDate(Long roomId, LocalDate startDateInclusive, LocalDate endDateInclusive) {
        return query
                .select(availability)
                .from(availability)
                .where(
                        roomId(roomId),
                        startBefore(endDateInclusive),
                        endAfter(startDateInclusive)
                )
                .fetch();
    }

    @Override
    public List<Availability> findTrueAvailabilitiesByDate(Long roomId, LocalDate startDateInclusive, LocalDate endDateInclusive) {
        return query
                .select(availability)
                .from(availability)
                .where(
                        roomId(roomId),
                        isAvailable(true),
                        startBefore(endDateInclusive),
                        endAfter(startDateInclusive)
                )
                .fetch();
    }

    @Override
    public List<Availability> findAvailabilitiesByMonth(Long roomId, YearMonth yearMonth) {
        return query
                .select(availability)
                .from(availability)
                .where(
                        roomId(roomId),
                        startBefore(yearMonth.atEndOfMonth()),
                        endAfter(yearMonth.atDay(1))
                )
                .orderBy(availability.startDate.asc())
                .fetch();
    }

    private BooleanExpression roomId(Long id) {
        return availability.room.id.eq(id);
    }

    private BooleanExpression startBefore(LocalDate endDate) {
        return availability.startDate.loe(endDate);
    }

    private BooleanExpression endAfter(LocalDate startDate) {
        return availability.endDate.goe(startDate);
    }

    private BooleanExpression isAvailable(boolean isAvailable) {
        return availability.isAvailable.eq(isAvailable);
    }
}
