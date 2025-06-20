package com.staybnb.bookings.domain;

import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.User;
import com.staybnb.rooms.domain.vo.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Double basePrice;

    @Column(nullable = false)
    private Double serviceFee;

    @Column(nullable = false)
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Booking(Long id, Room room, User user, LocalDate checkIn, LocalDate checkOut, Double basePrice, Double serviceFee, Double totalPrice, Currency currency, BookingStatus status) {
        this.id = id;
        this.room = room;
        this.user = user;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.basePrice = basePrice;
        this.serviceFee = serviceFee;
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.status = status;
    }
}
