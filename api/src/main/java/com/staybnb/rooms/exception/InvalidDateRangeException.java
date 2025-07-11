package com.staybnb.rooms.exception;

import java.time.LocalDate;

public class InvalidDateRangeException extends IllegalArgumentException {
    public InvalidDateRangeException(String message, LocalDate startDate, LocalDate endDate) {
        super(message + " startDate: " + startDate + ", endDate: " + endDate);
    }
}
