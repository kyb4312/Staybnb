package com.staybnb.common.exception.custom;

import java.util.NoSuchElementException;

public class NoSuchBookingException extends NoSuchElementException {
    public NoSuchBookingException() {
        super("존재하지 않는 예약입니다.");
    }
}
