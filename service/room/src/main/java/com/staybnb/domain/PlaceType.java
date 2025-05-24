package com.staybnb.domain;

import lombok.Getter;

public enum PlaceType {
    HOUSE("House"),
    APARTMENT("Apartment"),
    HOTEL("Hotel"),
    GUEST_HOUSE("Guest house"),
    ;

    @Getter
    private final String name;

    PlaceType(String name) {
        this.name = name;
    }

}
