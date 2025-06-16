package com.staybnb.rooms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pricing {

    public Pricing(Room room, LocalDate startDate, LocalDate endDate, Integer pricePerNight) {
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pricePerNight = pricePerNight;
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
    private Integer pricePerNight;

    private LocalDateTime updatedAt;
}
