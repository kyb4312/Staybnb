package com.staybnb.notification.domain;

import com.staybnb.users.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String content;

    private String url;

    @Setter
    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Notification(User user, String content) {
        this.user = user;
        this.content = content;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(User user, String content, String url) {
        this.user = user;
        this.content = content;
        this.url = url;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}
