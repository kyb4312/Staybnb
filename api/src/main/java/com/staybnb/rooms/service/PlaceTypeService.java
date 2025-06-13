package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.PlaceType;
import com.staybnb.rooms.repository.cache.PlaceTypeCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceTypeService {

    private final PlaceTypeCacheRepository placeTypeRepository;

    public PlaceType getByName(String name) {
        return placeTypeRepository.getByName(name);
    }
}
