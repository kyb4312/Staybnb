package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.PlaceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceTypeRepository extends JpaRepository<PlaceType, Long> {
}
