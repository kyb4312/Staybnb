package com.staybnb.rooms.exception;

import java.util.NoSuchElementException;

public class NoSuchRoomException extends NoSuchElementException {
    public NoSuchRoomException(Long roomId) {
        super("존재하지 않는 숙소입니다. roomId: " + roomId);
    }
}
