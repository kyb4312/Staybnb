package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.PlaceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceTypeRepository extends JpaRepository<PlaceType, Long> {

    Optional<PlaceType> findByName(String name);
}
