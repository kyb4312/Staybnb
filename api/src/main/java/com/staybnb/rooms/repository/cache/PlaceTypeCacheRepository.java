package com.staybnb.rooms.repository.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.staybnb.rooms.domain.PlaceType;
import com.staybnb.rooms.repository.PlaceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlaceTypeCacheRepository {

    private final PlaceTypeRepository placeTypeRepository;
    private final Cache<String, PlaceType> placeTypeCache = Caffeine.newBuilder().build();

    @Scheduled(initialDelay = 0, fixedDelay = 24 * 60 * 60 * 1000)
    public void loadPlaceTypeCache() {
        placeTypeRepository.findAll().forEach(placeType -> placeTypeCache.put(placeType.getName(), placeType));
    }

    public PlaceType getByName(String name) {
        PlaceType placeType = placeTypeCache.getIfPresent(name);
        if (placeType == null) {
            throw new IllegalArgumentException("PlaceType이 유효하지 않습니다.");
        }
        return placeTypeCache.getIfPresent(name);
    }
}
