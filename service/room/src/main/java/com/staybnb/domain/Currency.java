package com.staybnb.domain;

import lombok.Getter;

public enum Currency {
    KRW("KRW"),
    EUR("EUR"),
    USD("USD");

    @Getter
    private final String name;

    Currency(String name) {
        this.name = name;
    }
}
