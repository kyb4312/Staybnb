package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Availability;
import com.staybnb.rooms.repository.custom.AvailabilityRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, Long>, AvailabilityRepositoryCustom {
}
