package com.staybnb.rooms.controller;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import com.staybnb.rooms.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{roomId}")
    public RoomResponse getRoom(@PathVariable long roomId) {
        Room room = roomService.findById(roomId);
        return RoomResponse.fromDomain(room);
    }

    @GetMapping
    public Page<RoomResponse> getRooms(@Valid @ModelAttribute SearchRoomRequest searchRoomRequest, Pageable pageable) {
        return roomService.findAll(searchRoomRequest.toCommand(), pageable)
                .map(RoomResponse::fromDomain);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
        Room room = roomService.save(createRoomRequest.toCommand());
        URI location = UriComponentsBuilder
                .fromPath("/rooms/{roomId}")
                .buildAndExpand(room.getId())
                .toUri();

        log.debug("Room created: {}", room);
        log.debug("fromDomain: {}", RoomResponse.fromDomain(room));

        return ResponseEntity.created(location).body(RoomResponse.fromDomain(room));
    }

    @PatchMapping("/{roomId}")
    public RoomResponse updateRoom(@PathVariable long roomId, @Valid @RequestBody UpdateRoomRequest updateRoomRequest) {
        Room room = roomService.update(roomId, updateRoomRequest.toCommand());

        log.debug("Room updated: {}", room);
        log.debug("fromDomain: {}", RoomResponse.fromDomain(room));

        return RoomResponse.fromDomain(room);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable long roomId) {
        roomService.delete(roomId);
    }
}
