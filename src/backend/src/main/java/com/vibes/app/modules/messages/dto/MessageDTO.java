package com.vibes.app.modules.messages.dto;

import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.flyweight.MessageView;

import java.time.format.DateTimeFormatter;

/**
 * Unified message DTO that normalizes both Message and MessageView to a common format.
 * This abstraction allows the frontend to handle both private and group chat messages uniformly.
 */
public record MessageDTO(
    String id,
    String chatId,
    String senderId,
    String content,
    String type,
    String timestamp
) {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Creates a DTO from a regular Message entity (used for private chats).
     */
    public static MessageDTO from(Message message) {
        return new MessageDTO(
            message.getId(),
            message.getChatId(),
            message.getSenderId(),
            message.getContent(),
            message.getType(),
            message.getTimestamp() != null 
                ? message.getTimestamp().format(ISO_FORMATTER) 
                : null
        );
    }

    /**
     * Creates a DTO from a MessageView (used for group chats with flyweight pattern).
     */
    public static MessageDTO from(MessageView view) {
        return new MessageDTO(
            view.getMessageId(),
            view.getChatId(),
            view.getSenderId() != null ? view.getSenderId().toString() : null,
            view.getContent(),
            view.getType(),
            view.getTimestamp() != null 
                ? view.getTimestamp().format(ISO_FORMATTER) 
                : null
        );
    }
}
