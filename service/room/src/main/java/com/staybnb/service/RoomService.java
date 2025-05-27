package com.staybnb.service;

import com.staybnb.domain.Room;
import com.staybnb.domain.RoomSearchCondition;
import com.staybnb.domain.RoomUpdateInfo;
import com.staybnb.repository.RoomRepository;
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
