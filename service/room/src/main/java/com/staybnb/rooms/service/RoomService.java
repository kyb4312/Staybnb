package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.RoomSearchCondition;
import com.staybnb.rooms.dto.RoomUpdateInfo;
import com.staybnb.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public Room findById(long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public List<Room> findAll(RoomSearchCondition roomSearchCondition) {
        return roomRepository.findAll(roomSearchCondition);
    }

    public Room update(long id, RoomUpdateInfo roomUpdateInfo) {
        return roomRepository.update(id, roomUpdateInfo);
    }

    public void delete(long id) {
        roomRepository.delete(id);
    }
}
