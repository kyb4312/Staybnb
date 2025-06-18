package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.SearchRoomInfo;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final UserService userService;
    private final PlaceTypeService placeTypeService;
    private final AmenityService amenityService;
    private final ExchangeRateService exchangeRateService;

    public Room save(CreateRoomRequest request) {
        Room room = toEntity(request);
        room.setBasePriceInUsd(exchangeRateService.convertToUSD(room.getCurrency(), room.getBasePrice()));
        return roomRepository.save(room);
    }

    public Room findById(long id) {
        return roomRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 숙소입니다."));
    }

    public Page<Room> findAll(SearchRoomRequest request, Pageable pageable) {
        return roomRepository.findAll(toCommand(request), pageable);
    }

    public Room update(long id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 숙소입니다."));
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

    public void delete(long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 숙소입니다."));
        room.setDeleted(true);
        room.setDeletedAt(LocalDateTime.now());
    }

    private Room toEntity(CreateRoomRequest request) {
        return Room.builder()
                .host(userService.getById(request.getHostId()))
                .placeType(placeTypeService.getByName(request.getPlaceType()))
                .roomType(RoomType.valueOf(request.getRoomType()))
                .address(request.getAddress())
                .maxNumberOfGuests(request.getMaxNumberOfGuests())
                .bedrooms(request.getBedrooms())
                .beds(request.getBeds())
                .amenities(amenityService.getAmenitySetByStringSet(request.getAmenities()))
                .title(request.getTitle())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .currency(Currency.valueOf(request.getCurrency()))
                .build();
    }

    private SearchRoomInfo toCommand(SearchRoomRequest request) {
        return SearchRoomInfo.builder()
                .numberOfGuests(request.getGuests())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .priceFrom(request.getPriceFrom())
                .priceTo(request.getPriceTo())
                .currency(request.getCurrency())
                .build();
    }
}
