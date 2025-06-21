package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.dto.SearchRoomCondition;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.exception.NoSuchRoomException;
import com.staybnb.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final AmenityService amenityService;
    private final ExchangeRateService exchangeRateService;

    public Room save(Room room) {
        room.setBasePriceInUsd(exchangeRateService.convertToUSD(room.getCurrency(), room.getBasePrice()));
        return roomRepository.save(room);
    }

    public Room findById(long roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new NoSuchRoomException(roomId));
    }

    public Page<Room> findAll(SearchRoomCondition condition, Pageable pageable) {
        return roomRepository.findAll(condition, pageable);
    }

    @Transactional
    public Room update(long roomId, UpdateRoomRequest request) {
        Room room = findById(roomId);
        if(request.getMaxNumberOfGuests() != null) {
            room.setMaxNumberOfGuests(request.getMaxNumberOfGuests());
        }
        if(request.getBedrooms() != null) {
            room.setBedrooms(request.getBedrooms());
        }
        if(request.getBeds() != null) {
            room.setBeds(request.getBeds());
        }
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            room.setAmenities(amenityService.getAmenitySetByStringSet(request.getAmenities()));
        }
        if (request.getTitle() != null) {
            room.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            room.setBasePrice(request.getBasePrice());
            room.setBasePriceInUsd(exchangeRateService.convertToUSD(Currency.valueOf(request.getCurrency()), request.getBasePrice()));
        }
        if (request.getCurrency() != null) {
            room.setCurrency(Currency.valueOf(request.getCurrency()));
        }

        return room;
    }

    @Transactional
    public void delete(long roomId) {
        Room room = findById(roomId);
        room.setDeleted(true);
        room.setDeletedAt(LocalDateTime.now());
    }

}
