package com.staybnb.rooms.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final Cache<String, Amenity> amenityCache = Caffeine.newBuilder().build();

    @Scheduled(initialDelay = 0, fixedDelay = 24 * 60 * 60 * 1000)
    private void refreshAmenityCache() {
        amenityRepository.findAll().forEach(amenity -> amenityCache.put(amenity.getName(), amenity));
    }

    public Amenity getByName(String name) {
        return amenityCache.getIfPresent(name);
    }
}
