package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
}
