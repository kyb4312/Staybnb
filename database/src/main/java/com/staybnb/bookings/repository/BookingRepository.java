package com.staybnb.bookings.repository;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.rooms.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByRoom(Room room, Pageable pageable);

    @NativeQuery(
            value = """
                    SELECT * FROM booking
                    WHERE guest_id = :guestId
                        AND status in (:statuses)
                    ORDER BY date_range
                    """,
            countQuery = """
                    SELECT * FROM booking
                    WHERE guest_id = :guestId
                        AND status in (:statuses)
                    """
    )
    Page<Booking> findBookingsByGuestIdAndStatus(Pageable pageable, Long guestId, List<String> statuses);


    @NativeQuery(
            value = """
                    SELECT * FROM booking
                    WHERE status = :status
                    ORDER BY date_range
                    """,
            countQuery = """
                    SELECT * FROM booking
                    WHERE status = :status
                    """
    )
    Page<Booking> findBookingsByStatus(Pageable pageable, String status);
}
