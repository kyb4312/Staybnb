package com.staybnb.rooms.domain;

import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Type(PostgreSQLRangeType.class)
    @Column(nullable = false, columnDefinition = "daterange")
    private Range<LocalDate> dateRange;

    @Column(nullable = false)
    private Integer pricePerNight;

    private LocalDateTime updatedAt;

    public Pricing(Room room, LocalDate startDate, LocalDate endDate, Integer pricePerNight) {
        this.room = room;
        this.dateRange = Range.closedOpen(startDate, endDate);
        this.pricePerNight = pricePerNight;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDate getStartDate() {
        return dateRange.lower();
    }

    public LocalDate getEndDate() {
        return dateRange.upper();
    }
}
