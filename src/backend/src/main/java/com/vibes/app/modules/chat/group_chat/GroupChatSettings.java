package com.vibes.app.modules.chat.group_chat;

import jakarta.persistence.*;
import com.vibes.app.modules.chat.ChatSettings;

import java.util.UUID;

/**
 * Per-member settings for a group chat.
 * One row per (member, group) pair — created when a user joins the group.
 */
@Entity
@Table(name = "group_chat_settings")
public class GroupChatSettings implements ChatSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID chatId;

    @Column(nullable = false)
    private UUID userId;

    private boolean notificationsEnabled = true;

    public GroupChatSettings() {}

    @Override
    public void enableNotifications() { this.notificationsEnabled = true; }

    @Override
    public void disableNotifications() { this.notificationsEnabled = false; }

    @Override
    public void changeSettings() { this.notificationsEnabled = !this.notificationsEnabled; }

    public Long getId() { return id; }
    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) { this.chatId = chatId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}