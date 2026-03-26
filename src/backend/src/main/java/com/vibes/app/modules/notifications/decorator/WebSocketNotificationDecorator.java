package com.vibes.app.modules.notifications.decorator;

import com.vibes.app.modules.notifications.model.NotificationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class WebSocketNotificationDecorator implements NotificationSender {

  private final NotificationSender wrapped;
  private final SimpMessagingTemplate messagingTemplate;

  public WebSocketNotificationDecorator(
      NotificationSender wrapped,
      SimpMessagingTemplate messagingTemplate
  ) {
    this.wrapped = wrapped;
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public void send(NotificationContext notification) {
    System.out.println("[WebSocketNotificationDecorator] Sending notification to user: " + notification.getRecipientId());
    
    // Send to user's notification queue via WebSocket
    messagingTemplate.convertAndSendToUser(
        notification.getRecipientId().toString(),
        "/queue/notifications",
        notification
    );
    
    System.out.println("[WebSocketNotificationDecorator] Notification sent via WebSocket");

    // Continue the chain
    wrapped.send(notification);
  }
}
