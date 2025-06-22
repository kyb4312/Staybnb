package com.staybnb.bookings.repository.custom;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.vo.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingRepositoryCustom {

    Page<Booking> findBookingsByStatus(Pageable pageable, Long guestId, BookingStatus... status);
}
