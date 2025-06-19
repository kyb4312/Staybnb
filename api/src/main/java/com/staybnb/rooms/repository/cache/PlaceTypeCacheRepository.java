package com.staybnb.rooms.repository.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.staybnb.rooms.domain.PlaceType;
import com.staybnb.rooms.repository.PlaceTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class PlaceTypeCacheRepository {

    private final PlaceTypeRepository placeTypeRepository;
    private final LoadingCache<String, Optional<PlaceType>> placeTypeCache;

    public PlaceTypeCacheRepository(PlaceTypeRepository placeTypeRepository) {
        this.placeTypeRepository = placeTypeRepository;
        this.placeTypeCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(this.placeTypeRepository::findByName);
    }

    public PlaceType getByName(String name) {
        Optional<PlaceType> placeType = placeTypeCache.get(name);
        return placeType.orElseThrow(() -> new IllegalArgumentException("PlaceType이 유효하지 않습니다."));
    }
}
