package com.staybnb.rooms.exception;

public class InvalidRoomIdException extends IllegalArgumentException {
    public InvalidRoomIdException(Long roomId) {
        super("존재하지 않는 roomId 입니다. roomId : " + roomId);
    }
}
