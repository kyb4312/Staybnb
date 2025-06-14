package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.CreateRoomCommand;
import com.staybnb.rooms.dto.SearchRoomCommand;
import com.staybnb.rooms.dto.UpdateRoomCommand;
import com.staybnb.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CurrencyService currencyService;

    @Transactional
    public Room save(CreateRoomCommand room) {
        return roomRepository.save(commandToEntity(room));
    }

    public Room findById(long id) {
        return roomRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 숙소입니다."));
    }

    public Page<Room> findAll(SearchRoomCommand condition, Pageable pageable) {
        return roomRepository.findAll(condition, pageable);
    }

    @Transactional
    public Room update(long id, UpdateRoomCommand updateInfo) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 숙소입니다."));
        if(updateInfo.getMaxNumberOfGuests() != null) {
            room.setMaxNumberOfGuests(updateInfo.getMaxNumberOfGuests());
        }
        if(updateInfo.getBedrooms() != null) {
            room.setBedrooms(updateInfo.getBedrooms());
        }
        if(updateInfo.getBeds() != null) {
            room.setBeds(updateInfo.getBeds());
        }
        if (updateInfo.getAmenities() != null && !updateInfo.getAmenities().isEmpty()) {
            room.setAmenities(amenityService.getAmenitySetByStringSet(updateInfo.getAmenities()));
        }
        if (updateInfo.getTitle() != null) {
            room.setTitle(updateInfo.getTitle());
        }
        if (updateInfo.getDescription() != null) {
            room.setDescription(updateInfo.getDescription());
        }
        if (updateInfo.getPricePerNight() != null) {
            room.setPricePerNight(updateInfo.getPricePerNight());
        }
        if (updateInfo.getCurrency() != null) {
            room.setCurrency(currencyService.getByCode(updateInfo.getCurrency()));
        }

        return room;
    }

    @Transactional
    public void delete(long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 숙소입니다."));
        room.setDeleted(true);
        room.setDeletedAt(LocalDateTime.now());
    }

    private Room commandToEntity(CreateRoomCommand command) {
        return Room.builder()
                .host(userService.getById(command.getHostId()))
                .placeType(placeTypeService.getByName(command.getPlaceType()))
                .roomType(RoomType.valueOf(command.getRoomType()))
                .address(command.getAddress())
                .maxNumberOfGuests(command.getMaxNumberOfGuests())
                .bedrooms(command.getBedrooms())
                .beds(command.getBeds())
                .amenities(amenityService.getAmenitySetByStringSet(command.getAmenities()))
                .title(command.getTitle())
                .description(command.getDescription())
                .pricePerNight(command.getPricePerNight())
                .currency(currencyService.getByCode(command.getCurrency()))
                .build();
    }
}
