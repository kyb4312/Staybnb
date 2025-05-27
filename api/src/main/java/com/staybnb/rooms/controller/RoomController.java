package com.staybnb.rooms.controller;

import com.staybnb.domain.Room;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import com.staybnb.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable long roomId) {
        Room room = roomService.findById(roomId);
        return ResponseEntity.ok(RoomResponse.fromDomain(room));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRooms(@ModelAttribute SearchRoomRequest searchRoomRequest) {
        List<Room> rooms = roomService.findAll(searchRoomRequest.toDomain());
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : rooms) {
            roomResponses.add(RoomResponse.fromDomain(room));
        }

        return ResponseEntity.ok(roomResponses);
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
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable long roomId, @RequestBody UpdateRoomRequest updateRoomRequest) {
        Room room = roomService.update(roomId, updateRoomRequest.toDomain());
        return ResponseEntity.ok(RoomResponse.fromDomain(room));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable long roomId) {
        roomService.delete(roomId);
        return ResponseEntity.noContent().build();
    }
}
