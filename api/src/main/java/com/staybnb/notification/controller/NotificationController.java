package com.staybnb.notification.controller;

import com.staybnb.common.auth.dto.LoginUser;
import com.staybnb.notification.dto.response.NotificationResponse;
import com.staybnb.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public CompletableFuture<PagedModel<NotificationResponse>> getNotifications(LoginUser loginUser, Pageable pageable) {
        return notificationService.getNotifications(loginUser.getId(), pageable)
                .thenApply(pagedNotification -> new PagedModel<>(pagedNotification.map(NotificationResponse::fromEntity)));
    }

    @GetMapping("/unread")
    public CompletableFuture<PagedModel<NotificationResponse>> getUnreadNotifications(LoginUser loginUser, Pageable pageable) {
        return notificationService.getUnreadNotifications(loginUser.getId(), pageable)
                .thenApply(pagedNotification -> new PagedModel<>(pagedNotification.map(NotificationResponse::fromEntity)));
    }

    @PatchMapping("/{notificationId}")
    public void readNotification(@PathVariable Long notificationId, LoginUser loginUser) {
        notificationService.readNotification(loginUser.getId(), notificationId);
    }
}
