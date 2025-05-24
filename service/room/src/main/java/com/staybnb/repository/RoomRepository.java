package com.staybnb.repository;

import com.staybnb.domain.Room;
import com.staybnb.domain.RoomSearchCondition;
import com.staybnb.domain.RoomUpdateInfo;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    Room save(Room room);
    Room update(long id, RoomUpdateInfo roomUpdateInfo);
    Optional<Room> findById(long id);
    List<Room> findAll(RoomSearchCondition roomSearchCondition);
    void delete(long id);
}
