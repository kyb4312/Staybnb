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
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "availability_id_seq_gen")
    @SequenceGenerator(name = "availability_id_seq_gen", sequenceName = "availability_id_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Type(PostgreSQLRangeType.class)
    @Column(nullable = false, columnDefinition = "daterange")
    private Range<LocalDate> dateRange;

    @Column(nullable = false)
    private boolean isAvailable;

    private LocalDateTime updatedAt;

    public Availability(Room room, LocalDate startDate, LocalDate endDate, boolean isAvailable) {
        this.room = room;
        this.dateRange = Range.closedOpen(startDate, endDate);
        this.isAvailable = isAvailable;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDate getStartDate() {
        return dateRange.lower();
    }

    public LocalDate getEndDate() {
        return dateRange.upper();
    }

    public void setStartDate(LocalDate startDate) {
        this.dateRange = Range.closedOpen(startDate, this.dateRange.upper());
    }
}
