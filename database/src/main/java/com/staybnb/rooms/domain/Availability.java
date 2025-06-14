package com.staybnb.rooms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
public class Availability {

    public Availability() {
    }

    public Availability(Room room, LocalDate startDate, LocalDate endDate, boolean isAvailable) {
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAvailable = isAvailable;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean isAvailable;

    private LocalDateTime updatedAt;
}
