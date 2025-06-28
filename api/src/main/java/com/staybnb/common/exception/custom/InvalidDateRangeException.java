package com.staybnb.common.exception.custom;

import java.time.LocalDate;

public class InvalidDateRangeException extends IllegalArgumentException {
    public InvalidDateRangeException(String message, LocalDate startDate, LocalDate endDate) {
        super(message + " startDate: " + startDate + ", endDate: " + endDate);
    }
}
