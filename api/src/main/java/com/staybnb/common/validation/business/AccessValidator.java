package com.staybnb.common.validation.business;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.common.exception.custom.UnauthorizedException;
import com.staybnb.rooms.domain.Room;

public class AccessValidator {

    public static void validateHost(long userId, Room room) {
        if (!room.getHost().getId().equals(userId)) {
            throw new UnauthorizedException(userId);
        }
    }

    public static void validateHostOrGuest(long userId, Booking booking) {
        if (!booking.getRoom().getHost().getId().equals(userId) && !booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(userId);
        }
    }
}
