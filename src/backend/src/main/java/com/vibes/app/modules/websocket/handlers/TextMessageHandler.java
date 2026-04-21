package com.vibes.app.modules.websocket.handlers;

import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.services.MessageService;
import com.vibes.app.modules.notifications.service.NotificationService;
import com.vibes.app.modules.websocket.delivery.MessageDeliveryStrategy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextMessageHandler extends MessageHandler {

    public TextMessageHandler(MessageService messageService,
                              SimpMessagingTemplate messagingTemplate,
                              NotificationService notificationService,
                              List<MessageDeliveryStrategy> deliveryStrategies) {
        super(messageService, messagingTemplate, notificationService, deliveryStrategies);
    }

    @Override
    protected Message saveMessage(MessagePayload payload) throws Exception {
        return messageService.processAndSaveTextMessage(payload);
    }
}
