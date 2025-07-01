package com.staybnb.rooms.exception;

import java.util.NoSuchElementException;

public class NoSuchUserException extends NoSuchElementException {
    public NoSuchUserException(Long userId) {
        super("존재하지 않는 유저입니다. userId:" + userId);
    }
}
