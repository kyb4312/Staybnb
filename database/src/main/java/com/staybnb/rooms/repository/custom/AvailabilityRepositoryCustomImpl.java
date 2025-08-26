package com.staybnb.rooms.repository.custom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
public class AvailabilityRepositoryCustomImpl implements AvailabilityRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String CALL_UPDATE_ROOM_AVAILABILITY = "CALL update_room_availability(?, ?, ?)";

    @Override
    public void updateRoomAvailability(long roomId, List<String> dateRanges, boolean isAvailable) {
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(CALL_UPDATE_ROOM_AVAILABILITY)) {
                stmt.setLong(1, roomId);

                // PostgreSQL의 Array 타입으로 직접 변환
                Array sqlArray = connection.createArrayOf("daterange",
                        dateRanges.toArray(new String[0]));
                stmt.setArray(2, sqlArray);

                stmt.setBoolean(3, isAvailable);
                stmt.execute();
            }
        });
    }
}
