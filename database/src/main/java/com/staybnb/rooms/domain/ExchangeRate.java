package com.staybnb.rooms.domain;

import com.staybnb.rooms.domain.vo.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ExchangeRate {

    @Id
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    private Double rate;

    private LocalDateTime updatedAt;
}
