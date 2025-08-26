package com.staybnb.common.exception.custom;

import java.time.LocalDate;

public class UnavailableDateException extends RuntimeException {
    public UnavailableDateException(LocalDate checkIn, LocalDate checkOut) {
        super("예약이 불가한 날짜입니다. checkIn: " + checkIn + ", checkOut: " + checkOut);
    }
}
