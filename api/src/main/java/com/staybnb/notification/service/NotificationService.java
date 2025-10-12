package com.staybnb.notification.service;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.common.exception.custom.UnauthorizedException;
import com.staybnb.notification.domain.Notification;
import com.staybnb.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Executor asyncExecutor;

    public CompletableFuture<Page<Notification>> getNotifications(long userId, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> notificationRepository.findByUserId(userId, pageable), asyncExecutor);
    }

    public CompletableFuture<Page<Notification>> getUnreadNotifications(long userId, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> notificationRepository.findByUserIdAndIsReadFalse(userId, pageable), asyncExecutor);
    }

    public void readNotification(long userId, long notificationId) {
        CompletableFuture.runAsync(() -> {
            notificationRepository.findById(notificationId).ifPresent(notification -> {
                if (notification.getUser().getId() != userId) {
                    throw new UnauthorizedException(userId);
                }
                notification.setRead(true);
                notificationRepository.save(notification);
            });
        }, asyncExecutor);
    }

    public void createNotification(Booking booking) {
        CompletableFuture.runAsync(() -> {
            // TODO: 알림 구체화
            Notification notification = new Notification(booking.getRoom().getHost(), "예약 요청 알림");
            notificationRepository.save(notification);
        }, asyncExecutor);
    }
}
