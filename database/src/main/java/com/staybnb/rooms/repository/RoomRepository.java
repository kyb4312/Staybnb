package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.repository.custom.RoomRepositoryCustom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, RoomRepositoryCustom {

    @EntityGraph(attributePaths = {"placeType", "amenities"})
    @Query("SELECT r FROM Room r WHERE r.id = :roomId")
    Optional<Room> findByIdFetchJoin(long roomId);
}
