package com.staybnb.rooms.repository.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AmenityCacheRepository {

    private final AmenityRepository amenityRepository;
    private final Cache<String, Amenity> amenityCache = Caffeine.newBuilder().build();

    @Scheduled(initialDelay = 0, fixedDelay = 24 * 60 * 60 * 1000)
    public void refreshAmenityCache() {
        amenityRepository.findAll().forEach(amenity -> amenityCache.put(amenity.getName(), amenity));
    }

    public Amenity getByName(String name) {
        Amenity amenity = amenityCache.getIfPresent(name);
        if (amenity == null) {
            throw new IllegalArgumentException("Amenity가 유효하지 않습니다.");
        }
        return amenity;
    }
}
