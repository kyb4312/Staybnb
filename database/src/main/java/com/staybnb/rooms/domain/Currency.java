package com.staybnb.rooms.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    private String symbol;

    private Double exchangeRate;

    private LocalDateTime updatedAt;
}
