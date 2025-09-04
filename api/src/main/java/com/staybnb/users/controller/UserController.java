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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public CompletableFuture<UserResponse> login(@Valid @RequestBody LoginRequest request) {
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
    public CompletableFuture<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return userService.signup(toEntity(request))
                .thenApply(UserResponse::fromEntity);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CompletableFuture<Void> deleteAccount(LoginUser loginUser) {
        return userService.deleteAccount(loginUser.getId());
    }

    private User toEntity(SignupRequest request) {
        return new User(request.getEmail(), request.getName(), request.getPassword());
    }
}
