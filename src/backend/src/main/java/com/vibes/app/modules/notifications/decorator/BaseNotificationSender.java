package com.vibes.app.modules.notifications.decorator;

import com.vibes.app.modules.notifications.model.NotificationContext;
import org.springframework.stereotype.Component;

@Component
public class BaseNotificationSender implements NotificationSender {

  @Override
  public void send(NotificationContext notification) {
    // Base implementation - does nothing
    // Real work is done by decorators
  }
}
