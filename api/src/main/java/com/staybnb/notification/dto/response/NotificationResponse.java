package com.staybnb.notification.dto.response;

import com.staybnb.notification.domain.Notification;
import lombok.Getter;

@Getter
public class NotificationResponse {
    private long id;
    private String content;
    private String url;
    private boolean isRead;
    private String createdAt;

    public NotificationResponse(long id, String content, String url, boolean isRead, String createdAt) {
        this.id = id;
        this.content = content;
        this.url = url;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getContent(),
                notification.getUrl(),
                notification.isRead(),
                notification.getCreatedAt().toString()
        );
    }
}
