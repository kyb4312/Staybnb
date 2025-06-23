package com.staybnb.bookings.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.vo.BookingStatus;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.staybnb.bookings.domain.QBooking.booking;

public class BookingRepositoryCustomImpl implements BookingRepositoryCustom {

    private final JPAQueryFactory query;

    BookingRepositoryCustomImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<Booking> findBookingsByStatus(Pageable pageable, Long guestId, BookingStatus... status) {
        List<Booking> content = query
                .select(booking)
                .from(booking)
                .where(
                        guestId(guestId),
                        status(status)
                )
                .orderBy(booking.checkIn.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(booking.count())
                .from(booking)
                .where(
                        guestId(guestId),
                        status(status)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression guestId(Long guestId) {
        if (guestId == null) {
            return null;
        }
        return booking.user.id.eq(guestId);
    }

    private BooleanExpression status(BookingStatus... status) {
        return booking.status.in(status);
    }

}
