package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.*;
import com.staybnb.rooms.dto.RoomSearchCondition;
import com.staybnb.rooms.dto.RoomUpdateInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoomRepositoryTest {

    private final RoomRepository roomRepository = new RoomRepository();

    @Test
    void crudTest() {
        // save
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        List<Amenity> amenities = new ArrayList<>();
        amenities.add(Amenity.WIFI);
        amenities.add(Amenity.AIR_CONDITIONER);
        amenities.add(Amenity.TV);
        amenities.add(Amenity.KITCHEN);

        Room room = Room.builder()
                .hostId(1L)
                .placeType(PlaceType.HOUSE)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern house in Kentucky")
                .description("Modern house in Kentucky")
                .pricePerNight(700_000)
                .currency(Currency.KRW)
                .build();

        Room roomCreated = roomRepository.save(room);
        long roomId = roomCreated.getId();

        // findById
        Room roomFound = roomRepository.findById(roomId).orElse(null);
        assertThat(roomFound).isEqualTo(roomCreated);

        // findAll
        // 검색 조건에 맞는 숙소 찾음
        RoomSearchCondition searchCondition = RoomSearchCondition.builder()
                .numberOfGuests(2)
                .location("Kentucky")
                .build();
        List<Room> rooms = roomRepository.findAll(searchCondition);
        assertThat(rooms).hasSize(1);

        // 검색 조건에 맞는 숙소 없음
        searchCondition = RoomSearchCondition.builder()
                .numberOfGuests(2)
                .priceTo(500_000)
                .build();
        rooms = roomRepository.findAll(searchCondition);
        assertThat(rooms).hasSize(0);

        // update
        RoomUpdateInfo updateInfo = RoomUpdateInfo.builder()
                .maxNumberOfGuests(4)
                .bedrooms(2)
                .beds(2)
                .pricePerNight(800_000)
                .build();

        Room roomUpdated = roomRepository.update(roomId, updateInfo);

        assertThat(roomUpdated.getId()).isEqualTo(roomId);
        assertThat(roomUpdated.getMaxNumberOfGuests()).isEqualTo(updateInfo.getMaxNumberOfGuests());
        assertThat(roomUpdated.getBedrooms()).isEqualTo(updateInfo.getBedrooms());
        assertThat(roomUpdated.getBeds()).isEqualTo(updateInfo.getBeds());
        assertThat(roomUpdated.getPricePerNight()).isEqualTo(updateInfo.getPricePerNight());

        // delete
        Room roomDeleted = roomRepository.findById(roomId).orElse(null);
        assertThat(roomDeleted).isNotNull();
        assertThat(roomDeleted.isDeleted()).isFalse();

        roomRepository.delete(roomId);

        roomDeleted = roomRepository.findById(roomId).orElse(null);
        assertThat(roomDeleted).isNotNull();
        assertThat(roomDeleted.isDeleted()).isTrue();
        assertThat(roomDeleted.getDeletedAt()).isNotNull();
    }
}