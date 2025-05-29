package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.vo.*;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.RoomSearchCondition;
import com.staybnb.rooms.dto.RoomUpdateInfo;
import com.staybnb.rooms.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Captor
    ArgumentCaptor<Room> roomCaptor;

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

        // when
        roomService.save(room);

        // then
        verify(roomRepository, times(1)).save(room);

        verify(roomRepository).save(roomCaptor.capture());
        Room savedRoom = roomCaptor.getValue();

        assertThat(savedRoom)
                .usingRecursiveComparison()
                .isEqualTo(room);
    }

    @Test
    @DisplayName("findById(): 숙소 등록 후, id로 해당 숙소 찾기")
    void findById() {
        // given
        long roomId = 1L;

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

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // when
        Room foundRoom = roomService.findById(roomId);

        // then
        verify(roomRepository, times(1)).findById(roomId);

        assertThat(foundRoom)
                .usingRecursiveComparison()
                .isEqualTo(room);
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

        Room room = Room.builder()
                .id(1L)
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

        // 최대 숙박 인원에 따른 검색
        RoomSearchCondition searchCondition = RoomSearchCondition.builder().numberOfGuests(2).build();

        when(roomRepository.findAll(searchCondition)).thenReturn(List.of(room));

        // when
        List<Room> rooms = roomService.findAll(searchCondition);

        // then
        verify(roomRepository, times(1)).findAll(any(RoomSearchCondition.class));
        assertThat(rooms.size()).isEqualTo(1);
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

        Room expected = Room.builder()
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
                .pricePerNight(updateInfo.getPricePerNight()) // updated
                .currency(Currency.KRW)
                .build();

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        // when
        roomService.update(room.getId(), updateInfo);

        // then
        verify(roomRepository, times(1)).update(room.getId(), room);

        verify(roomRepository).update(eq(room.getId()), roomCaptor.capture());
        Room updatedRoom = roomCaptor.getValue();

        assertThat(updatedRoom)
                .usingRecursiveComparison()
                .isEqualTo(expected);
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

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        // when
        roomService.delete(roomId);

        // then
        verify(roomRepository, times(1)).delete(room);
        assertThat(room.isDeleted()).isTrue();
        assertThat(room.getDeletedAt()).isNotNull();
    }
}