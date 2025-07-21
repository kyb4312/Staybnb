package com.staybnb.bookings.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "timezone_midnight")
public class TimezoneMidnight {

    @Id
    private String timeZoneId;

    @Column(nullable = false)
    private LocalTime utcMidnight;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public TimezoneMidnight() {
    }

    public TimezoneMidnight(String timeZoneId, LocalTime utcMidnight) {
        this.timeZoneId = timeZoneId;
        this.utcMidnight = utcMidnight;
    }
}
