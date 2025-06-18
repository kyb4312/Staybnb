package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.repository.cache.AmenityCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityCacheRepository amenityRepository;

    public Set<Amenity> getAmenitySetByStringSet(Set<String> amenities) {
        return amenities.stream().map(amenityRepository::getByName).collect(Collectors.toSet());
    }
}
