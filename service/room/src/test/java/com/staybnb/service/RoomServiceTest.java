package com.staybnb.service;

import com.staybnb.domain.*;
import com.staybnb.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    RoomRepository roomRepository;

    @InjectMocks
    RoomService roomService;

    @Test
    @DisplayName("save(): 숙소 등록 시 id가 포함된 객체 반환")
    void save() {
        // given
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        List<Amenity> amenities = new ArrayList<>();
        amenities.add(Amenity.WIFI);
        amenities.add(Amenity.KITCHEN);
        amenities.add(Amenity.AIR_CONDITIONER);
        amenities.add(Amenity.TV);

        Room room = Room.builder()
                .hostId(1L)
                .placeType(PlaceType.HOUSE)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency(Currency.KRW)
                .build();

        when(roomRepository.save(room)).thenAnswer(invocation -> {
            Room requestedRoom = invocation.getArgument(0);
            requestedRoom.setId(1L);
            return requestedRoom;
        });

        // when
        Room savedRoom = roomService.save(room);

        // then
        verify(roomRepository, times(1)).save(room);
        assertThat(savedRoom.getId()).isNotNull();
        assertThat(savedRoom.getTitle()).isEqualTo(room.getTitle());
        assertThat(savedRoom.getPricePerNight()).isEqualTo(room.getPricePerNight());
    }

    @Test
    @DisplayName("findById(): 숙소 등록 후, id로 해당 숙소 찾기")
    void findById() {
        // given
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        List<Amenity> amenities = new ArrayList<>();
        amenities.add(Amenity.WIFI);
        amenities.add(Amenity.KITCHEN);
        amenities.add(Amenity.AIR_CONDITIONER);
        amenities.add(Amenity.TV);

        Room room = Room.builder()
                .hostId(1L)
                .placeType(PlaceType.HOUSE)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency(Currency.KRW)
                .build();

        when(roomRepository.save(room)).thenAnswer(invocation -> {
            Room requestedRoom = invocation.getArgument(0);
            requestedRoom.setId(1L);
            return requestedRoom;
        });

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // when
        Room savedRoom = roomService.save(room);
        Room foundRoom = roomService.findById(savedRoom.getId());

        // then
        verify(roomRepository, times(1)).save(room);
        verify(roomRepository, times(1)).findById(savedRoom.getId());
        assertThat(foundRoom.getId()).isEqualTo(savedRoom.getId());
        assertThat(foundRoom.getTitle()).isEqualTo(savedRoom.getTitle());
        assertThat(foundRoom.getPricePerNight()).isEqualTo(savedRoom.getPricePerNight());
    }

    @Test
    @DisplayName("finaAll(): 최대 숙박 인원으로 숙소 검색")
    void findAll() {
        // given
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        List<Amenity> amenities = new ArrayList<>();
        amenities.add(Amenity.WIFI);
        amenities.add(Amenity.KITCHEN);
        amenities.add(Amenity.AIR_CONDITIONER);
        amenities.add(Amenity.TV);

        Room room1 = Room.builder()
                .hostId(1L)
                .placeType(PlaceType.HOUSE)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2) // 최대 인원 2명
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency(Currency.KRW)
                .build();

        Room room2 = Room.builder()
                .hostId(1L)
                .placeType(PlaceType.HOUSE)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(4) // 최대 인원 4명
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency(Currency.KRW)
                .build();

        // 최대 숙박 인원에 따른 검색
        RoomSearchCondition searchCondition1 = RoomSearchCondition.builder().numberOfGuests(2).build();
        RoomSearchCondition searchCondition2 = RoomSearchCondition.builder().numberOfGuests(4).build();
        RoomSearchCondition searchCondition3 = RoomSearchCondition.builder().numberOfGuests(5).build();

        when(roomRepository.findAll(searchCondition1)).thenReturn(List.of(room1, room2));
        when(roomRepository.findAll(searchCondition2)).thenReturn(List.of(room2));
        when(roomRepository.findAll(searchCondition3)).thenReturn(List.of());

        // when
        List<Room> rooms1 = roomService.findAll(searchCondition1);
        List<Room> rooms2 = roomService.findAll(searchCondition2);
        List<Room> rooms3 = roomService.findAll(searchCondition3);

        // then
        verify(roomRepository, times(3)).findAll(any(RoomSearchCondition.class));
        assertThat(rooms1.size()).isEqualTo(2);
        assertThat(rooms2.size()).isEqualTo(1);
        assertThat(rooms3.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("update(): 기본 숙박 가격 정보 수정")
    void update() {
        // given
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        List<Amenity> amenities = new ArrayList<>();
        amenities.add(Amenity.WIFI);
        amenities.add(Amenity.KITCHEN);
        amenities.add(Amenity.AIR_CONDITIONER);
        amenities.add(Amenity.TV);

        long roomId = 1L;

        Room room = Room.builder()
                .id(roomId)
                .hostId(1L)
                .placeType(PlaceType.HOUSE)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency(Currency.KRW)
                .build();

        RoomUpdateInfo updateInfo = RoomUpdateInfo.builder()
                .pricePerNight(800_000)
                .build();

        when(roomRepository.update(room.getId(), updateInfo)).thenAnswer(invocation -> {
            room.updatePrice(updateInfo.getPricePerNight());
            return room;
        });

        // when
        Room updatedRoom = roomService.update(room.getId(), updateInfo);

        // then
        verify(roomRepository, times(1)).update(room.getId(), updateInfo);
        assertThat(updatedRoom.getId()).isEqualTo(room.getId());
        assertThat(updatedRoom.getTitle()).isEqualTo(room.getTitle());
        assertThat(updatedRoom.getPricePerNight()).isEqualTo(room.getPricePerNight());
    }

    @Test
    @DisplayName("delete(): 숙소 삭제")
    void delete() {
        // given
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        List<Amenity> amenities = new ArrayList<>();
        amenities.add(Amenity.WIFI);
        amenities.add(Amenity.KITCHEN);
        amenities.add(Amenity.AIR_CONDITIONER);
        amenities.add(Amenity.TV);

        long roomId = 1L;

        Room room = Room.builder()
                .id(roomId)
                .hostId(1L)
                .placeType(PlaceType.HOUSE)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency(Currency.KRW)
                .build();

        doAnswer(invocation -> {
            room.delete(LocalDateTime.now());
            return null;
        }).when(roomRepository).delete(roomId);

        // when
        roomService.delete(roomId);

        // then
        verify(roomRepository, times(1)).delete(roomId);
        assertThat(room.isDeleted()).isTrue();
        assertThat(room.getDeletedAt()).isNotNull();
    }
}