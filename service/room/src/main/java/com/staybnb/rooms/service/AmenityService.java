package com.staybnb.rooms.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

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
        Amenity amenity = amenityCache.getIfPresent(name);
        if (amenity == null) {
            throw new IllegalArgumentException("Amenity가 유효하지 않습니다.");
        }
        return amenity;
    }

    public Set<Amenity> getAmenitySetByStringSet(Set<String> amenities) {
        return amenities.stream().map(this::getByName).collect(Collectors.toSet());
    }
}
