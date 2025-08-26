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
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Executor asyncExecutor;

    @PostMapping("/login")
    public CompletableFuture<UserResponse> login(@Valid @RequestBody LoginRequest request) {
//        log.info("step: controller entry → {}", Thread.currentThread().getName());
        return CompletableFuture.supplyAsync(() -> userService.login(request.getEmail(), request.getPassword()), asyncExecutor);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException();
        }
        String token = authorization.substring(7);

        CompletableFuture.runAsync(() -> userService.logout(token), asyncExecutor);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return CompletableFuture
                .supplyAsync(() -> userService.signup(toEntity(request)), asyncExecutor)
                .thenApply(UserResponse::fromEntity);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(LoginUser loginUser) {
        CompletableFuture.runAsync(() -> userService.deleteAccount(loginUser.getId()), asyncExecutor);
    }

    private User toEntity(SignupRequest request) {
        return new User(request.getEmail(), request.getName(), request.getPassword());
    }
}
