package com.staybnb.domain;

import lombok.Getter;

public enum Amenity {
    WIFI("Wi-Fi"),
    KITCHEN("Kitchen"),
    AIR_CONDITIONER("Air Conditioner"),
    TV("TV"),
    FREE_PARKING("Free Parking"),
    ;

    @Getter
    private final String name;

    Amenity(String name) {
        this.name = name;
    }
}
