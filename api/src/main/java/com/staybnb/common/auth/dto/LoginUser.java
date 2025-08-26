package com.staybnb.common.auth.dto;

import lombok.Getter;

@Getter
public class LoginUser {

    private final Long id;
    private final String name;

    public LoginUser(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
