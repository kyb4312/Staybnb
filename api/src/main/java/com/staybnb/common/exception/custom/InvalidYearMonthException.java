package com.staybnb.common.exception.custom;

import java.time.YearMonth;

public class InvalidYearMonthException extends IllegalArgumentException {
    public InvalidYearMonthException(String message, YearMonth yearMonth) {
        super(message + " yearMonth: " + yearMonth);
    }
}
