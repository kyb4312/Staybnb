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

    @Override
    public void updateRoomAvailability(long roomId, List<String> dateRanges, boolean isAvailable) {
        String sql = "CALL update_room_availability(?, ?, ?)";

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
