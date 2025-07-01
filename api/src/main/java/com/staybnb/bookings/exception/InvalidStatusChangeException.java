package com.staybnb.bookings.exception;

public class InvalidStatusChangeException extends IllegalArgumentException {

    public InvalidStatusChangeException() {
        super("숙소 상태를 변경할 수 없습니다.");
    }

    public InvalidStatusChangeException(String currentStatus) {
        super("숙소 상태를 변경할 수 없습니다. currentStatus: " + currentStatus);
    }
}
