package com.vibes.app.modules.websocket.handlers;

import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.services.MessageService;
import com.vibes.app.modules.notifications.service.NotificationService;
import com.vibes.app.modules.websocket.delivery.MessageDeliveryStrategy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

public abstract class MessageHandler {

    protected final MessageService messageService;
    protected final SimpMessagingTemplate messagingTemplate;
    protected final NotificationService notificationService;
    protected final List<MessageDeliveryStrategy> deliveryStrategies;

    protected MessageHandler(MessageService messageService,
                             SimpMessagingTemplate messagingTemplate,
                             NotificationService notificationService,
                             List<MessageDeliveryStrategy> deliveryStrategies) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.deliveryStrategies = deliveryStrategies;
    }

    @Transactional
    public final void handle(MessagePayload payload, Principal principal) throws Exception {
        UUID senderId = UUID.fromString(principal.getName());
        payload.setSenderId(senderId.toString());

        Message saved = saveMessage(payload);

        MessageDeliveryStrategy strategy = deliveryStrategies.stream()
                .filter(s -> s.supports(payload.getChatId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported chat type or chat not found for ID: " + payload.getChatId()));

        List<UUID> recipients = strategy.resolveRecipients(payload.getChatId(), senderId);
        Object outboundPayload = strategy.buildPayload(saved, senderId);

        strategy.deliver(outboundPayload, recipients, messagingTemplate);

        recipients.stream()
                .filter(recipientId -> !recipientId.equals(senderId))
                .forEach(recipientId -> notificationService.notifyNewMessage(saved, recipientId));
    }

    protected abstract Message saveMessage(MessagePayload payload) throws Exception;
}
