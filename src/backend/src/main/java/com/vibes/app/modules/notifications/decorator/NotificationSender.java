package com.vibes.app.modules.notifications.decorator;

import com.vibes.app.modules.notifications.model.NotificationContext;

public interface NotificationSender {
  void send(NotificationContext notification);
}
