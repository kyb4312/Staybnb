package com.staybnb.users.controller;

import com.staybnb.common.auth.dto.LoginUser;
import com.staybnb.common.exception.custom.UnauthorizedException;
import com.staybnb.users.domain.User;
import com.staybnb.users.dto.request.SignupRequest;
import com.staybnb.users.dto.response.UserResponse;
import com.staybnb.users.service.UserService;
import com.staybnb.users.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException();
        }
        String token = authorization.substring(7);

        userService.logout(token);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest request) {
        return UserResponse.fromEntity(userService.signup(toEntity(request)));
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(LoginUser loginUser) {
        userService.deleteAccount(loginUser.getId());
    }

    private User toEntity(SignupRequest request) {
        return new User(request.getEmail(), request.getName(), request.getPassword());
    }
}
