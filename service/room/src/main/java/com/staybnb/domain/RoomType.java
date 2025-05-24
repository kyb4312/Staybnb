package com.staybnb.domain;

import lombok.Getter;

public enum RoomType {
    ENTIRE_PLACE("An entire place"),
    A_ROOM("A room"),
    SHARED_ROOM("A shared room"),
    ;

    @Getter
    private final String name;

    RoomType(String name) {
        this.name = name;
    }
}
