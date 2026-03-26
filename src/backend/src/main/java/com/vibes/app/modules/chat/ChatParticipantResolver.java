package com.vibes.app.modules.chat;

import java.util.List;
import java.util.UUID;

/**
 * Resolves participants for typing notifications in a chat.
 * Abstracts away the differences between private and group chats.
 */
public interface ChatParticipantResolver {
    /**
     * Returns all participants in the chat except the sender.
     * For private chats: returns the other user.
     * For group chats: returns all members except the sender.
     */
    List<UUID> resolveRecipients(UUID chatId, UUID senderId);
    
    /**
     * Checks if this resolver can handle the given chat ID.
     */
    boolean canResolve(UUID chatId);
}
