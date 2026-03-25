package com.vibes.app.modules.chat.private_chat;

import jakarta.persistence.*;
import com.vibes.app.modules.chat.ChatSettings;

import java.util.UUID;

@Entity
@Table(name = "chat_settings")
public class PrivateChatSettings implements ChatSettings {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private UUID chatId;

  private boolean notificationsEnabled = true;

  public PrivateChatSettings() {
  }

  @Override
  public void enableNotifications() {
    this.notificationsEnabled = true;
  }

  @Override
  public void disableNotifications() {
    this.notificationsEnabled = false;
  }

  // TODO: IMPLEMENT THIS FUNCTION IT WAS CAUSING A BUILD ERROR
  @Override
  public void changeSettings() {}

  public Long getId() {
    return id;
  }

  public UUID getChatId() {
    return chatId;
  }

  public void setChatId(UUID chatId) {
    this.chatId = chatId;
  }

  public boolean isNotificationsEnabled() {
    return notificationsEnabled;
  }

  public void setNotificationsEnabled(boolean notificationsEnabled) {
    this.notificationsEnabled = notificationsEnabled;
  }
}
