package com.staybnb.users.dto.response;

import com.staybnb.users.domain.User;
import lombok.Getter;

@Getter
public class UserResponse {

    private Long id;
    private String email;
    private String name;

    public UserResponse(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}