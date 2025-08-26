package com.staybnb.common.exception.custom;

import java.time.LocalDate;

public class InvalidDateRangeException extends IllegalArgumentException {
    public InvalidDateRangeException(String message, LocalDate startDate, LocalDate endDate) {
        super(String.format("%s [%s, %s]", message, startDate, endDate));
    }

    public InvalidDateRangeException(String message, LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2) {
        super(String.format("%s [%s, %s], [%s, %s]", message, startDate1, endDate1, startDate2, endDate2));
    }
}
