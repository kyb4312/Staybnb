package com.staybnb.rooms.repository.custom;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.SearchRoomCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface RoomRepositoryCustom {
    Page<Room> findAll(SearchRoomCommand roomSearchCondition, Pageable pageable);
}
