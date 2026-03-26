package com.vibes.app.modules.notifications.model;

import com.vibes.app.modules.auth.models.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_read_status")
public class ChatReadStatus {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "chat_id", nullable = false)
  private UUID chatId;

  @Column(name = "last_read_message_id")
  private UUID lastReadMessageId;

  @Column(name = "unread_count", nullable = false)
  private Integer unreadCount = 0;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public ChatReadStatus() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public UUID getChatId() {
    return chatId;
  }

  public void setChatId(UUID chatId) {
    this.chatId = chatId;
  }

  public UUID getLastReadMessageId() {
    return lastReadMessageId;
  }

  public void setLastReadMessageId(UUID lastReadMessageId) {
    this.lastReadMessageId = lastReadMessageId;
  }

  public Integer getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(Integer unreadCount) {
    this.unreadCount = unreadCount;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
