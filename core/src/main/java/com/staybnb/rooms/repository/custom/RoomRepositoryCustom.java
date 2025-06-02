package com.staybnb.rooms.repository.custom;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.SearchRoomCommand;

import java.util.List;

public interface RoomRepositoryCustom {
    List<Room> findAll(SearchRoomCommand roomSearchCondition);
}
