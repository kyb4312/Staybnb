package com.staybnb.rooms.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Currency {

    public Currency(String code, String name, Double exchangeRate) {
        this.code = code;
        this.name = name;
        this.exchangeRate = exchangeRate;
    }

    @Id
    private String code;

    private String name;

    @Column(nullable = false)
    private Double exchangeRate;

    private LocalDateTime updatedAt;
}
