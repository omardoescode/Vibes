package com.vibes.app.modules.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatResponse {
    private UUID chatId;
    private UUID otherUserId;
    private String otherUsername;
    private String otherUserProfilePicture;
    private LocalDateTime createdAt;

    public ChatResponse(UUID chatId, UUID otherUserId, String otherUsername,
                        String otherUserProfilePicture, LocalDateTime createdAt) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.otherUserProfilePicture = otherUserProfilePicture;
        this.createdAt = createdAt;
    }

    public UUID getChatId() { return chatId; }
    public UUID getOtherUserId() { return otherUserId; }
    public String getOtherUsername() { return otherUsername; }
    public String getOtherUserProfilePicture() { return otherUserProfilePicture; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
