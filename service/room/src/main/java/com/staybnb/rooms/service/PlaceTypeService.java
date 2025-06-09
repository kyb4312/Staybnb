package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.PlaceType;
import com.staybnb.rooms.repository.PlaceTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceTypeService {

    private final PlaceTypeRepository placeTypeRepository;
    private final Map<String, PlaceType> placeTypeMap = new HashMap<>();

    @PostConstruct
    public void loadPlaceTypeMap() {
        placeTypeRepository.findAll().forEach(placeType -> placeTypeMap.put(placeType.getName(), placeType));
        log.debug("PlaceTypeMap: {}", placeTypeMap);
    }

    public PlaceType getByName(String name) {
        log.debug("getByName : {}", name);
        log.debug("placeTypeMap Result : {}", placeTypeMap.get(name));
        return placeTypeMap.get(name);
    }
}
