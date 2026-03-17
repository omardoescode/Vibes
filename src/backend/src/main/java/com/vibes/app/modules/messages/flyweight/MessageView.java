package com.vibes.app.modules.messages.flyweight;

import com.vibes.app.modules.messages.entities.Message;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only view of a message enriched with sender profile data.
 *
 * Combines:
 *   - Extrinsic per-message context (messageId, chatId, content, type, timestamp)
 *     — unique to each message instance
 *   - Intrinsic sender data from {@link UserProfileFlyweight}
 *     (senderId, senderUsername, senderProfilePictureUrl) — shared / cached
 *   - Extrinsic runtime status (senderStatus) — passed in at render time,
 *     never cached because it changes frequently
 *
 * Used for group chat message responses so the frontend receives full sender
 * info without extra HTTP calls, while the backend avoids redundant DB lookups.
 */
public class MessageView {

    // Extrinsic — unique per message
    private final String messageId;
    private final String chatId;
    private final String content;
    private final String type;
    private final LocalDateTime timestamp;

    // Intrinsic — from shared UserProfileFlyweight
    private final UUID senderId;
    private final String senderUsername;
    private final String senderProfilePictureUrl;

    // Extrinsic runtime — NOT cached
    private final String senderStatus;

    public MessageView(Message message, UserProfileFlyweight flyweight, String senderStatus) {
        this.messageId = message.getId();
        this.chatId = message.getChatId();
        this.content = message.getContent();
        this.type = message.getType();
        this.timestamp = message.getTimestamp();
        this.senderId = flyweight.getSenderId();
        this.senderUsername = flyweight.getSenderUsername();
        this.senderProfilePictureUrl = flyweight.getSenderProfilePictureUrl();
        this.senderStatus = senderStatus;
    }

    public String getMessageId() { return messageId; }
    public String getChatId() { return chatId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public UUID getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public String getSenderProfilePictureUrl() { return senderProfilePictureUrl; }
    public String getSenderStatus() { return senderStatus; }
}