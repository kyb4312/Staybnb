package com.staybnb.bookings.repository;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.repository.custom.BookingRepositoryCustom;
import com.staybnb.rooms.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long>, BookingRepositoryCustom {

    Page<Booking> findByRoom(Room room, Pageable pageable);
}
