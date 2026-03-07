package com.vibes.app.modules.websocket;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry that tracks, for each connected user, which chat they
 * currently have open in their browser.
 *
 * Lifecycle:
 * - openChat(userId, chatId) — called when a user opens / switches to a chat
 * - closeChat(userId) — called when a user closes a chat or disconnects
 * - getOpenChat(userId) — returns the chatId the user is currently viewing
 * - getUsersInChat(chatId) — returns all user IDs currently viewing that chat
 */
@Component
public class ChatSessionRegistry {

  // userId → chatId they have open right now
  private final ConcurrentHashMap<String, String> userToChat = new ConcurrentHashMap<>();

  // chatId → set of userIds currently viewing it
  private final ConcurrentHashMap<String, Set<String>> chatToUsers = new ConcurrentHashMap<>();

  /**
   * Record that {@code userId} has opened {@code chatId}.
   * Automatically removes any previously open chat for that user.
   */
  public void openChat(String userId, String chatId) {
    // Remove from old chat first
    String previous = userToChat.put(userId, chatId);
    if (previous != null && !previous.equals(chatId)) {
      Set<String> oldViewers = chatToUsers.get(previous);
      if (oldViewers != null)
        oldViewers.remove(userId);
    }

    chatToUsers
        .computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
        .add(userId);
  }

  /**
   * Record that {@code userId} has closed their active chat (or disconnected).
   */
  public void closeChat(String userId) {
    String chatId = userToChat.remove(userId);
    if (chatId != null) {
      Set<String> viewers = chatToUsers.get(chatId);
      if (viewers != null)
        viewers.remove(userId);
    }
  }

  /**
   * Returns the chatId currently open for {@code userId}, if any.
   */
  public Optional<String> getOpenChat(String userId) {
    return Optional.ofNullable(userToChat.get(userId));
  }

  /**
   * Returns the set of user IDs currently viewing {@code chatId}.
   * Returns an empty set if nobody is viewing it.
   */
  public Set<String> getUsersInChat(String chatId) {
    return chatToUsers.getOrDefault(chatId, Collections.emptySet());
  }

  /**
   * Returns true if {@code userId} currently has {@code chatId} open.
   */
  public boolean isUserInChat(String userId, String chatId) {
    return chatId.equals(userToChat.get(userId));
  }
}
