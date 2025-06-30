package com.staybnb.bookings.domain;

import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.rooms.domain.Room;
import com.staybnb.users.domain.User;
import com.staybnb.rooms.domain.vo.Currency;
import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private User user;

    @Column(nullable = false)
    private Integer numberOfGuests;

    @Type(PostgreSQLRangeType.class)
    @Column(nullable = false, columnDefinition = "daterange")
    private Range<LocalDate> dateRange;

    @Column(nullable = false)
    private Double bookingPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Booking() {
    }

    public Booking(Room room, User user, Integer numberOfGuests, LocalDate checkIn, LocalDate checkOut, Double bookingPrice, Currency currency) {
        this.room = room;
        this.user = user;
        this.numberOfGuests = numberOfGuests;
        this.dateRange = Range.closedOpen(checkIn, checkOut);
        this.bookingPrice = bookingPrice;
        this.currency = currency;
    }

    public LocalDate getCheckIn() {
        return dateRange.lower();
    }

    public LocalDate getCheckOut() {
        return dateRange.upper();
    }
}
