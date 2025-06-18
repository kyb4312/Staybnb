package com.staybnb.rooms.repository.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.repository.AmenityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class AmenityCacheRepository {

    private final AmenityRepository amenityRepository;
    private final LoadingCache<String, Optional<Amenity>> amenityCache;

    public AmenityCacheRepository(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
        this.amenityCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(this.amenityRepository::findByName);
    }

    public Amenity getByName(String name) {
        Optional<Amenity> amenity = amenityCache.get(name);
        if (amenity.isEmpty()) {
            throw new IllegalArgumentException("Amenity가 유효하지 않습니다.");
        }
        return amenity.get();
    }
}
