package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.domain.Currency;
import com.staybnb.rooms.domain.PlaceType;
import com.staybnb.rooms.domain.User;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.SearchRoomInfo;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.repository.RoomRepository;
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

    @Mock
    RoomRepository roomRepository;

    @InjectMocks
    RoomService roomService;

    @Mock
    UserService userService;

    @Mock
    PlaceTypeService placeTypeService;

    @Mock
    AmenityService amenityService;

    @Mock
    CurrencyService currencyService;

    @Captor
    ArgumentCaptor<Room> roomCaptor;

    @Test
    @DisplayName("save(): 숙소 등록 시 id가 포함된 객체 반환")
    void save() {
        // given
        Long hostId = 1L;
        String placeType = "house";
        String currency = "KRW";
        Set<String> amenities = Set.of("wifi", "tv");

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        CreateRoomRequest room = CreateRoomRequest.builder()
                .hostId(hostId)
                .placeType(placeType)
                .roomType("ENTIRE_PLACE")
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency(currency)
                .build();

        // when
        roomService.save(room);

        // then
        verify(userService, times(1)).getById(hostId);
        verify(placeTypeService, times(1)).getByName(placeType);
        verify(amenityService, times(1)).getAmenitySetByStringSet(amenities);
        verify(currencyService, times(1)).getByCode(currency);
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

        User user = new User();
        user.setId(1L);

        PlaceType placeType = new PlaceType();
        placeType.setId(1);

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Amenity amenity = new Amenity();
        amenity.setId(1);
        Amenity amenity2 = new Amenity();
        amenity.setId(2);
        Set<Amenity> amenities = Set.of(amenity, amenity2);

        Currency currency = new Currency();
        currency.setCode("KRW");

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
                .currency(currency)
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

        User user = new User();
        user.setId(1L);

        PlaceType placeType = new PlaceType();
        placeType.setId(1);

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Amenity amenity = new Amenity();
        amenity.setId(1);
        Amenity amenity2 = new Amenity();
        amenity.setId(2);
        Set<Amenity> amenities = Set.of(amenity, amenity2);

        Currency currency = new Currency();
        currency.setCode("KRW");

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
                .currency(currency)
                .build();

        SearchRoomRequest searchRoomRequest = SearchRoomRequest.builder().guests(2).build();
        Page<Room> pageResponse= new PageImpl<>(List.of(room));
        when(roomRepository.findAll(any(SearchRoomInfo.class), eq(null))).thenReturn(pageResponse);

        // when
        Page<Room> rooms = roomService.findAll(searchRoomRequest, null);

        // then
        verify(roomRepository, times(1)).findAll(any(SearchRoomInfo.class), eq(null));
        assertThat(rooms.getContent().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("update(): 기본 숙박 가격 정보 수정")
    void update() {
        // given
        long roomId = 1L;

        User user = new User();
        user.setId(1L);

        PlaceType placeType = new PlaceType();
        placeType.setId(1);

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Amenity amenity = new Amenity();
        amenity.setId(1);
        Amenity amenity2 = new Amenity();
        amenity.setId(2);
        Set<Amenity> amenities = Set.of(amenity, amenity2);

        Currency currency = new Currency();
        currency.setCode("KRW");

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
                .currency(currency)
                .build();

        UpdateRoomRequest updateInfo = UpdateRoomRequest.builder()
                .basePrice(800_000)
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
                .currency(currency)
                .build();

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        // when
        Room updatedRoom = roomService.update(room.getId(), updateInfo);

        // then
        verify(roomRepository, times(1)).findById(room.getId());

        assertThat(updatedRoom)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("delete(): 숙소 삭제")
    void delete() {
        // given
        long roomId = 1L;

        User user = new User();
        user.setId(1L);

        PlaceType placeType = new PlaceType();
        placeType.setId(1);

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Amenity amenity = new Amenity();
        amenity.setId(1);
        Amenity amenity2 = new Amenity();
        amenity.setId(2);
        Set<Amenity> amenities = Set.of(amenity, amenity2);

        Currency currency = new Currency();
        currency.setCode("KRW");

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
                .currency(currency)
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