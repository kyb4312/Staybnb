package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.RoomSearchCondition;
import com.staybnb.rooms.dto.RoomUpdateInfo;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    Room save(Room room);
    Room update(long id, RoomUpdateInfo roomUpdateInfo);
    Optional<Room> findById(long id);
    List<Room> findAll(RoomSearchCondition roomSearchCondition);
    void delete(long id);
}
