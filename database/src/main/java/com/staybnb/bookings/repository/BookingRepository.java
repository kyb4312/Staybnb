package com.staybnb.bookings.repository;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.repository.custom.BookingRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long>, BookingRepositoryCustom {
}
