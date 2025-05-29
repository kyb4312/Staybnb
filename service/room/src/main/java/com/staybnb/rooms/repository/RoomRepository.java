package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.RoomSearchCondition;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class RoomRepository{

    private static final Map<Long, Room> storage = new ConcurrentHashMap<>();
    private static final AtomicLong sequence = new AtomicLong(1);

    public Room save(Room room) {
        room.setId(sequence.getAndIncrement());
        storage.put(room.getId(), room);
        return room;
    }

    public Room update(long id, Room room) {
        storage.put(id, room);
        return room;
    }

    public Optional<Room> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

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
                .filter(room -> ObjectUtils.isEmpty(guests) || room.getMaxNumberOfGuests() <= guests)
                .filter(room -> ObjectUtils.isEmpty(startDate) || true) // TODO: reservation 기능 구현 후 예약 가능 일자 필터링 예정
                .filter(room -> ObjectUtils.isEmpty(endDate) || true) // TODO: reservation 기능 구현 후 예약 가능 일자 필터링 예정
                .filter(room -> ObjectUtils.isEmpty(location) || room.getAddress().contains(location))
                .filter(room -> ObjectUtils.isEmpty(currency) || true) // TODO: currency 전환 방법 고민 필요
                .filter(room -> ObjectUtils.isEmpty(priceFrom) || room.getPricePerNight() >= priceFrom)
                .filter(room -> ObjectUtils.isEmpty(priceTo) || room.getPricePerNight() <= priceTo)
                .collect(Collectors.toList());
    }

    public void delete(Room room) {
        storage.put(room.getId(), room);
    }
}
