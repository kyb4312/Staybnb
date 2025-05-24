package com.staybnb.repository;

import com.staybnb.domain.Currency;
import com.staybnb.domain.Room;
import com.staybnb.domain.RoomSearchCondition;
import com.staybnb.domain.RoomUpdateInfo;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MemoryRoomRepository implements RoomRepository {

    private static final Map<Long, Room> storage = new ConcurrentHashMap<>();
    private static long sequence = 0L;

    @Override
    public Room save(Room room) {
        room.setId(++sequence);
        storage.put(room.getId(), room);
        return room;
    }

    @Override
    public Room update(long id, RoomUpdateInfo roomUpdateInfo) {
        Room room = findById(id).orElseThrow();
        if(roomUpdateInfo.getMaxNumberOfGuests() != null) {
            room.updateMaxNumberOfGuests(roomUpdateInfo.getMaxNumberOfGuests());
        }
        if(roomUpdateInfo.getBedrooms() != null) {
            room.updateBedrooms(roomUpdateInfo.getBedrooms());
        }
        if(roomUpdateInfo.getBeds() != null) {
            room.updateBeds(roomUpdateInfo.getBeds());
        }
        if(roomUpdateInfo.getAmenities() != null) {
            room.updateAmenities(roomUpdateInfo.getAmenities());
        }
        if(roomUpdateInfo.getTitle() != null) {
            room.updateTitle(roomUpdateInfo.getTitle());
        }
        if(roomUpdateInfo.getDescription() != null) {
            room.updateDescription(roomUpdateInfo.getDescription());
        }
        if(roomUpdateInfo.getPricePerNight() != null) {
            room.updatePrice(roomUpdateInfo.getPricePerNight());
        }
        if(roomUpdateInfo.getCurrency() != null) {
            room.updateCurrency(roomUpdateInfo.getCurrency());
        }

        return room;
    }

    @Override
    public Optional<Room> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Room> findAll(RoomSearchCondition roomSearchCondition) {
        Integer guests = roomSearchCondition.getNumberOfGuests();
        LocalDate startDate = roomSearchCondition.getStartDate();
        LocalDate endDate = roomSearchCondition.getEndDate();
        String location = roomSearchCondition.getLocation();
        Integer priceFrom = roomSearchCondition.getPriceFrom();
        Integer priceTo = roomSearchCondition.getPriceTo();
        Currency currency = roomSearchCondition.getCurrency();

        return storage.values().stream()
                .filter(room -> !room.isDeleted())
                .filter(room -> {
                    if(ObjectUtils.isEmpty(guests)) {
                        return true;
                    }
                    return room.getMaxNumberOfGuests() <= guests;
                }).filter(room -> {
                    if(ObjectUtils.isEmpty(startDate)) {
                        return true;
                    }
                    return true; // TODO: reservation 기능 구현 후 예약 가능 일자 필터링 예정
                }).filter(room -> {
                    if(ObjectUtils.isEmpty(endDate)) {
                        return true;
                    }
                    return true; // TODO: reservation 기능 구현 후 예약 가능 일자 필터링 예정
                }).filter(room -> {
                    if(ObjectUtils.isEmpty(location)) {
                        return true;
                    }
                    return room.getAddress().contains(location);
                }).filter(room -> {
                    if(ObjectUtils.isEmpty(currency)) {
                         return true;
                    }
                    return true; // TODO: currency 전환 방법 고민 필요
                }).filter(room -> {
                    if(ObjectUtils.isEmpty(priceFrom)) {
                        return true;
                    }
                    return room.getPricePerNight() >= priceFrom;
                }).filter(room -> {
                    if(ObjectUtils.isEmpty(priceTo)) {
                        return true;
                    }
                    return room.getPricePerNight() <= priceTo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void delete(long id) {
        Room room = findById(id).orElseThrow();
        room.delete(LocalDateTime.now());
    }
}
