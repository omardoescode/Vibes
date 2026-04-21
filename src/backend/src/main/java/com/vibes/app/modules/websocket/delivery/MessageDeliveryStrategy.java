package com.vibes.app.modules.websocket.delivery;

import com.vibes.app.modules.messages.entities.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import java.util.UUID;

public interface MessageDeliveryStrategy {
    boolean supports(String chatId);
    Object buildPayload(Message message, UUID senderId);
    List<UUID> resolveRecipients(String chatId, UUID senderId);
    void deliver(Object payload, List<UUID> recipients, SimpMessagingTemplate template);
}
