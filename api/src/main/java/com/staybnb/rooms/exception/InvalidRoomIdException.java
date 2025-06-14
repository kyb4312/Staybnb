package com.staybnb.rooms.exception;

public class InvalidRoomIdException extends RuntimeException {
    public InvalidRoomIdException(Long roomId) {
        super("Invalid Room Id: " + roomId);
    }
}
