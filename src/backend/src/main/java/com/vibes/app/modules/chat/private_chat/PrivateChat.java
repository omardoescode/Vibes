package com.vibes.app.modules.chat.private_chat;

import jakarta.persistence.*;
import com.vibes.app.modules.chat.Chat;
import com.vibes.app.modules.auth.models.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "private_chats")
public class PrivateChat implements Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PrivateChat() {}

    public PrivateChat(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    @Override
    public String getChatId() {
        return id != null ? id.toString() : null;
    }

    public UUID getId() { return id; }
    public User getUser1() { return user1; }
    public void setUser1(User user1) { this.user1 = user1; }
    public User getUser2() { return user2; }
    public void setUser2(User user2) { this.user2 = user2; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
