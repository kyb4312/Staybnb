package com.staybnb.common.exception.custom;

public class InvalidTimeZoneIdException extends IllegalArgumentException {
    public InvalidTimeZoneIdException(String timeZoneId) {
        super("timeZoneId가 유효하지 않습니다: " + timeZoneId);
    }
}
