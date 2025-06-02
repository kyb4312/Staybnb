package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.repository.AmenityRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final Map<String, Amenity> amenityMap = new HashMap<>();

    @PostConstruct
    public void loadAmenityMap() {
        amenityRepository.findAll().forEach(amenity -> amenityMap.put(amenity.getName(), amenity));
    }

    public Amenity getByName(String name) {
        return amenityMap.get(name);
    }
}
