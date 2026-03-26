package com.vibes.app.modules.notifications.decorator;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import com.vibes.app.modules.notifications.model.ChatReadStatus;
import com.vibes.app.modules.notifications.model.NotificationContext;
import com.vibes.app.modules.notifications.model.NotificationType;
import com.vibes.app.modules.notifications.repository.ChatReadStatusRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class UnreadCountDecorator implements NotificationSender {

  private final NotificationSender wrapped;
  private final ChatReadStatusRepository chatReadStatusRepository;
  private final UserRepository userRepository;

  public UnreadCountDecorator(
      NotificationSender wrapped,
      ChatReadStatusRepository chatReadStatusRepository,
      UserRepository userRepository
  ) {
    this.wrapped = wrapped;
    this.chatReadStatusRepository = chatReadStatusRepository;
    this.userRepository = userRepository;
  }

  @Override
  public void send(NotificationContext notification) {
    // Update unread count in database for NEW_MESSAGE notifications
    if (notification.getType() == NotificationType.NEW_MESSAGE) {
      incrementUnreadCount(notification.getRecipientId(), notification.getChatId());
    }

    // Continue the chain
    wrapped.send(notification);
  }

  private void incrementUnreadCount(UUID userId, UUID chatId) {
    Optional<ChatReadStatus> existing = chatReadStatusRepository.findByUserIdAndChatId(userId, chatId);

    if (existing.isPresent()) {
      ChatReadStatus status = existing.get();
      status.setUnreadCount(status.getUnreadCount() + 1);
      status.setUpdatedAt(LocalDateTime.now());
      chatReadStatusRepository.save(status);
    } else {
      // Create new read status entry
      Optional<User> user = userRepository.findById(userId);
      if (user.isPresent()) {
        ChatReadStatus newStatus = new ChatReadStatus();
        newStatus.setUser(user.get());
        newStatus.setChatId(chatId);
        newStatus.setUnreadCount(1);
        newStatus.setUpdatedAt(LocalDateTime.now());
        chatReadStatusRepository.save(newStatus);
      }
    }
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
}
