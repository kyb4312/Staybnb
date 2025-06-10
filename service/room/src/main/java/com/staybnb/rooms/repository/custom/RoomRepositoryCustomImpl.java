package com.staybnb.rooms.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.SearchRoomCommand;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.staybnb.rooms.domain.QRoom.room;

@Repository
public class RoomRepositoryCustomImpl implements RoomRepositoryCustom {

    private final JPAQueryFactory query;

    public RoomRepositoryCustomImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<Room> findAll(SearchRoomCommand cond, Pageable pageable) {
        List<Room> content = query
                .select(room)
                .from(room)
                .where(
                        // TODO: startDate, endDate, currency 조건 추가해야 함
                        numberOfGuests(cond.getNumberOfGuests()),
                        containsLocation(cond.getLocation()),
                        minPrice(cond.getPriceFrom()),
                        maxPrice(cond.getPriceTo())
                )
                .orderBy(room.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(room.count())
                .from(room)
                .where(
                        numberOfGuests(cond.getNumberOfGuests()),
                        containsLocation(cond.getLocation()),
                        minPrice(cond.getPriceFrom()),
                        maxPrice(cond.getPriceTo())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression numberOfGuests(Integer numberOfGuests) {
        if (numberOfGuests != null) {
            return room.maxNumberOfGuests.goe(numberOfGuests);
        }
        return null;
    }

    private BooleanExpression containsLocation(String location) {
        if (StringUtils.hasText(location)) {
            return room.address.country.containsIgnoreCase(location)
                    .or(room.address.province.containsIgnoreCase(location))
                    .or(room.address.city.containsIgnoreCase(location))
                    .or(room.address.street.containsIgnoreCase(location));
        }
        return null;
    }

    private BooleanExpression minPrice(Integer priceFrom) {
        if (priceFrom != null) {
            return room.pricePerNight.goe(priceFrom);
        }
        return null;
    }

    private BooleanExpression maxPrice(Integer priceTo) {
        if (priceTo != null) {
            return room.pricePerNight.loe(priceTo);
        }
        return null;
    }
}
