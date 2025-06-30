package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.*;
import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.SearchRoomCondition;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.repository.RoomRepository;
import com.staybnb.users.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @InjectMocks
    RoomService roomService;

    @Mock
    RoomRepository roomRepository;

    @Mock
    ExchangeRateService exchangeRateService;

    @Captor
    ArgumentCaptor<Room> roomCaptor;

    @Test
    @DisplayName("save(): 숙소 등록 시 id가 포함된 객체 반환")
    void save() {
        // given
        User host = new User("host@gmail.com", "host", "password");
        PlaceType placeType = new PlaceType(1, "HOUSE");
        Set<Amenity> amenities = Set.of();

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Room room = Room.builder()
                .host(host)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency(Currency.KRW)
                .build();

        when(exchangeRateService.convertToUSD(Currency.KRW, room.getBasePrice())).thenReturn(700_000 / 1350.0);

        // when
        roomService.save(room);

        // then
        verify(exchangeRateService, times(1)).convertToUSD(Currency.KRW, room.getBasePrice());
        verify(roomRepository, times(1)).save(any(Room.class));

        verify(roomRepository).save(roomCaptor.capture());
        Room savedRoom = roomCaptor.getValue();

        Room expected = Room.builder()
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency(Currency.KRW)
                .basePriceInUsd(700_000 / 1350.0)
                .build();

        assertThat(savedRoom)
                .usingRecursiveComparison()
                .ignoringFields("id", "host", "placeType", "amenities", "currency")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("findById(): 숙소 등록 후, id로 해당 숙소 찾기")
    void findById() {
        // given
        long roomId = 1L;

        User user = new User("user@gmail.com", "user", "password");
        user.setId(1L);

        PlaceType placeType = new PlaceType(1, "house");
        Set<Amenity> amenities = Set.of(new Amenity(1, "wifi"), new Amenity(2, "tv"));

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Room room = Room.builder()
                .id(roomId)
                .host(user)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
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
        long roomId = 1L;

        User user = new User("user@gmail.com", "user", "password");
        user.setId(1L);

        PlaceType placeType = new PlaceType(1, "house");
        Set<Amenity> amenities = Set.of(new Amenity(1, "wifi"), new Amenity(2, "tv"));

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Room room = Room.builder()
                .id(roomId)
                .host(user)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency(Currency.KRW)
                .build();

        SearchRoomCondition searchRoomCondition = SearchRoomCondition.builder().numberOfGuests(2).build();
        Page<Room> pageResponse= new PageImpl<>(List.of(room));
        when(roomRepository.findAll(any(SearchRoomCondition.class), eq(null))).thenReturn(pageResponse);

        // when
        Page<Room> rooms = roomService.findAll(searchRoomCondition, null);

        // then
        verify(roomRepository, times(1)).findAll(any(SearchRoomCondition.class), eq(null));
        assertThat(rooms.getContent().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("update(): 기본 숙박 가격 정보 수정")
    void update() {
        // given
        long roomId = 1L;

        User user = new User("user@gmail.com", "user", "password");
        user.setId(1L);

        PlaceType placeType = new PlaceType(1, "house");
        Set<Amenity> amenities = Set.of(new Amenity(1, "wifi"), new Amenity(2, "tv"));

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Room room = Room.builder()
                .id(roomId)
                .host(user)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency(Currency.KRW)
                .build();

        UpdateRoomRequest updateInfo = UpdateRoomRequest.builder()
                .basePrice(800_000)
                .currency("KRW")
                .build();

        Room expected = Room.builder()
                .id(roomId)
                .host(user)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(updateInfo.getBasePrice()) // updated
                .currency(Currency.KRW)
                .basePriceInUsd(updateInfo.getBasePrice() / 1350.0)
                .build();

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        // when
        Room updatedRoom = roomService.update(room.getId(), updateInfo);

        // then
        verify(roomRepository, times(1)).findById(room.getId());

        assertThat(updatedRoom)
                .usingRecursiveComparison()
                .ignoringFields("basePriceInUsd")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("delete(): 숙소 삭제")
    void delete() {
        // given
        long roomId = 1L;

        User user = new User("user@gmail.com", "user", "password");
        user.setId(1L);

        PlaceType placeType = new PlaceType(1, "house");
        Set<Amenity> amenities = Set.of(new Amenity(1, "wifi"), new Amenity(2, "tv"));

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Room room = Room.builder()
                .id(roomId)
                .host(user)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency(Currency.KRW)
                .build();

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        // when
        roomService.delete(roomId);

        // then
        verify(roomRepository, times(1)).findById(room.getId());
        assertThat(room.isDeleted()).isTrue();
        assertThat(room.getDeletedAt()).isNotNull();
    }
}