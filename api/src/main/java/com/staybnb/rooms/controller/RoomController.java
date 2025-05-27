package com.staybnb.rooms.controller;

import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import com.staybnb.rooms.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<RoomResponse> getRooms(@ModelAttribute SearchRoomRequest searchRoomRequest) {
        return roomService.findAll(searchRoomRequest.toDomain())
                .stream()
                .map(RoomResponse::fromDomain)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody CreateRoomRequest createRoomRequest) {
        Room room = roomService.save(createRoomRequest.toDomain());
        URI location = UriComponentsBuilder
                .fromPath("/rooms/{roomId}")
                .buildAndExpand(room.getId())
                .toUri();

        return ResponseEntity.created(location).body(RoomResponse.fromDomain(room));
    }

    @PatchMapping("/{roomId}")
    public RoomResponse updateRoom(@PathVariable long roomId, @RequestBody UpdateRoomRequest updateRoomRequest) {
        Room room = roomService.update(roomId, updateRoomRequest.toDomain());
        return RoomResponse.fromDomain(room);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable long roomId) {
        roomService.delete(roomId);
    }
}
