package com.vibes.app.modules.notifications.service;

import com.vibes.app.modules.auth.repositories.UserRepository;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.notifications.decorator.BaseNotificationSender;
import com.vibes.app.modules.notifications.decorator.NotificationSender;
import com.vibes.app.modules.notifications.decorator.UnreadCountDecorator;
import com.vibes.app.modules.notifications.decorator.WebSocketNotificationDecorator;
import com.vibes.app.modules.notifications.model.ChatReadStatus;
import com.vibes.app.modules.notifications.model.NotificationContext;
import com.vibes.app.modules.notifications.model.NotificationType;
import com.vibes.app.modules.notifications.repository.ChatReadStatusRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

  private final SimpMessagingTemplate messagingTemplate;
  private final ChatReadStatusRepository chatReadStatusRepository;
  private final UserRepository userRepository;

  public NotificationService(
      SimpMessagingTemplate messagingTemplate,
      ChatReadStatusRepository chatReadStatusRepository,
      UserRepository userRepository
  ) {
    this.messagingTemplate = messagingTemplate;
    this.chatReadStatusRepository = chatReadStatusRepository;
    this.userRepository = userRepository;
  }

  /**
   * Factory method to build the decorator chain.
   * Chain order (outer to inner): UnreadCount -> WebSocket -> Base
   */
  private NotificationSender getNotificationChain() {
    NotificationSender chain = new BaseNotificationSender();
    chain = new WebSocketNotificationDecorator(chain, messagingTemplate);
    chain = new UnreadCountDecorator(chain, chatReadStatusRepository, userRepository);
    return chain;
  }

  public void notifyNewMessage(Message message, UUID recipientId) {
    System.out.println("[NotificationService] notifyNewMessage called for recipient: " + recipientId);

    NotificationContext ctx = new NotificationContext();
    ctx.setType(NotificationType.NEW_MESSAGE);
    ctx.setRecipientId(recipientId);
    ctx.setSenderId(UUID.fromString(message.getSenderId()));
    ctx.setChatId(UUID.fromString(message.getChatId()));
    ctx.setTitle("New Message");
    ctx.setBody("You have a new message");
    ctx.setTimestamp(LocalDateTime.now());
    ctx.setPayload(message);

    System.out.println("[NotificationService] Sending notification context: " + ctx.getType() + " to " + recipientId);
    getNotificationChain().send(ctx);
    System.out.println("[NotificationService] Notification sent successfully");
  }

  public void notifyTyping(UUID chatId, UUID senderId, UUID recipientId, boolean isTyping) {
    NotificationContext ctx = new NotificationContext();
    ctx.setType(isTyping ? NotificationType.TYPING_START : NotificationType.TYPING_STOP);
    ctx.setRecipientId(recipientId);
    ctx.setSenderId(senderId);
    ctx.setChatId(chatId);
    ctx.setTitle(isTyping ? "Typing..." : "");
    ctx.setBody(isTyping ? "Someone is typing..." : "");
    ctx.setTimestamp(LocalDateTime.now());

    getNotificationChain().send(ctx);
  }

  public void notifyUserStatus(UUID recipientId, UUID userId, String username, boolean isOnline) {
    NotificationContext ctx = new NotificationContext();
    ctx.setType(isOnline ? NotificationType.USER_ONLINE : NotificationType.USER_OFFLINE);
    ctx.setRecipientId(recipientId);
    ctx.setSenderId(userId);
    ctx.setTitle(isOnline ? "User Online" : "User Offline");
    ctx.setBody(username + " is now " + (isOnline ? "online" : "offline"));
    ctx.setTimestamp(LocalDateTime.now());

    getNotificationChain().send(ctx);
  }

  /**
   * Notifies a group member that a new user has been added to the group.
   *
   * @param groupId      the group that was modified
   * @param addedUserId  the user who was added
   * @param recipientId  the existing member to notify
   */
  public void notifyMemberAdded(UUID groupId, UUID addedUserId, UUID recipientId) {
    NotificationContext ctx = new NotificationContext();
    ctx.setType(NotificationType.MEMBER_ADDED);
    ctx.setRecipientId(recipientId);
    ctx.setSenderId(addedUserId);
    ctx.setChatId(groupId);
    ctx.setTitle("New group member");
    ctx.setBody("A new member has been added to the group");
    ctx.setTimestamp(LocalDateTime.now());

    getNotificationChain().send(ctx);
  }

  /**
   * Notifies a group member that someone has been removed from the group.
   *
   * @param groupId        the group that was modified
   * @param removedUserId  the user who was removed
   * @param recipientId    the remaining member to notify
   */
  public void notifyMemberRemoved(UUID groupId, UUID removedUserId, UUID recipientId) {
    NotificationContext ctx = new NotificationContext();
    ctx.setType(NotificationType.MEMBER_REMOVED);
    ctx.setRecipientId(recipientId);
    ctx.setSenderId(removedUserId);
    ctx.setChatId(groupId);
    ctx.setTitle("Group member removed");
    ctx.setBody("A member has been removed from the group");
    ctx.setTimestamp(LocalDateTime.now());

    getNotificationChain().send(ctx);
  }

  public void resetUnreadCount(UUID userId, UUID chatId) {
    Optional<ChatReadStatus> existing = chatReadStatusRepository.findByUserIdAndChatId(userId, chatId);
    if (existing.isPresent()) {
      ChatReadStatus status = existing.get();
      status.setUnreadCount(0);
      status.setUpdatedAt(LocalDateTime.now());
      chatReadStatusRepository.save(status);
    }
  }

  public int getUnreadCount(UUID userId, UUID chatId) {
    return chatReadStatusRepository.findByUserIdAndChatId(userId, chatId)
        .map(ChatReadStatus::getUnreadCount)
        .orElse(0);
  }

  public java.util.Map<UUID, Integer> getAllUnreadCounts(UUID userId) {
    java.util.Map<UUID, Integer> counts = new java.util.HashMap<>();
    for (ChatReadStatus status : chatReadStatusRepository.findByUserId(userId)) {
      counts.put(status.getChatId(), status.getUnreadCount());
    }
    return counts;
  }
}