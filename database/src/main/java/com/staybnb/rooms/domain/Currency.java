package com.staybnb.rooms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Currency {

    @Id
    private String code;

    private String name;

    @Column(nullable = false)
    private Double exchangeRate;

    private LocalDateTime updatedAt;
}
