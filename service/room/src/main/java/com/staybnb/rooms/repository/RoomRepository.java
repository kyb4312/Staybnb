package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.repository.custom.RoomRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long>, RoomRepositoryCustom {
}
