package com.staybnb.bookings.exception;

public class ExceededNumberOfGuestException extends RuntimeException {
    public ExceededNumberOfGuestException(int maxNumberOfGuests, int numberOfGuests) {
        super("최대 숙박 인원 초과입니다. maxNumberOfGusts: " + maxNumberOfGuests + ", numberOfGusts: " + numberOfGuests);
    }
}
