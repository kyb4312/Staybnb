package com.staybnb.rooms.service;

import com.staybnb.common.exception.custom.InvalidTimeZoneIdException;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.dto.SearchRoomCondition;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.common.exception.custom.NoSuchRoomException;
import com.staybnb.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import static com.staybnb.common.validation.business.AccessValidator.validateHost;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final AmenityService amenityService;
    private final ExchangeRateService exchangeRateService;

    @Async
    public CompletableFuture<Room> save(Room room) {
        validateTimeZoneId(room.getTimeZoneId());
        room.setBasePriceInUsd(exchangeRateService.convertToUSD(room.getCurrency(), room.getBasePrice()));
        return CompletableFuture.completedFuture(roomRepository.save(room));
    }

    @Async
    public CompletableFuture<Room> getRoom(long roomId) {
//        log.info("step: service entry → {}", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(findByIdFetchJoin(roomId));
    }

    @Async
    public CompletableFuture<Page<Room>> findAll(SearchRoomCondition condition, Pageable pageable) {
        log.info("step: service entry → {}", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(roomRepository.findAll(condition, pageable));
    }

    @Async
    @Transactional
    public CompletableFuture<Room> update(long userId, long roomId, UpdateRoomRequest request) {
        Room room = findByIdFetchJoin(roomId);
        validateHost(userId, room);

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

        return CompletableFuture.completedFuture(room);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> delete(long userId, long roomId) {
        Room room = findById(roomId);
        validateHost(userId, room);

        room.setDeleted(true);
        room.setDeletedAt(LocalDateTime.now());

        return CompletableFuture.completedFuture(null);
    }

    public Room findById(long roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new NoSuchRoomException(roomId));
    }

    public Room findByIdFetchJoin(long roomId) {
        return roomRepository.findByIdFetchJoin(roomId).orElseThrow(() -> new NoSuchRoomException(roomId));
    }

    private void validateTimeZoneId(String timeZoneId) {
        try {
            ZoneId.of(timeZoneId);
        } catch (Exception e) {
            throw new InvalidTimeZoneIdException(timeZoneId);
        }
    }

}
