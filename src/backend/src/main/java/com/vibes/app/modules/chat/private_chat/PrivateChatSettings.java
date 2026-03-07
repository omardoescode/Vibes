package com.vibes.app.modules.chat.private_chat;

import jakarta.persistence.*;
import com.vibes.app.modules.chat.ChatSettings;

@Entity
@Table(name = "chat_settings")
public class PrivateChatSettings implements ChatSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private boolean notificationsEnabled;

    @Override
    public void changeSettings() {
        System.out.println("Changing private chat settings");
    }

    @Override
    public void enableNotifications() {
        this.notificationsEnabled = true;
        System.out.println("Notifications enabled");
    }

    @Override
    public void disableNotifications() {
        this.notificationsEnabled = false;
        System.out.println("Notifications disabled");
    }

    public Long getId() { return id; }
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
}