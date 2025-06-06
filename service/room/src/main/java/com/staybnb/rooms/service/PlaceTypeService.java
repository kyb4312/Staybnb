package com.staybnb.rooms.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.staybnb.rooms.domain.PlaceType;
import com.staybnb.rooms.repository.PlaceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceTypeService {

    private final PlaceTypeRepository placeTypeRepository;
    private final Cache<String, PlaceType> placeTypeCache = Caffeine.newBuilder().build();

    @Scheduled(initialDelay = 0, fixedDelay = 24 * 60 * 60 * 1000)
    public void loadPlaceTypeCache() {
        placeTypeRepository.findAll().forEach(placeType -> placeTypeCache.put(placeType.getName(), placeType));
    }

    public PlaceType getByName(String name) {
        return placeTypeCache.getIfPresent(name);
    }
}
