package com.vibes.app.modules.websocket.delivery;

import com.vibes.app.modules.chat.repositories.PrivateChatRepository;
import com.vibes.app.modules.messages.dto.MessageDTO;
import com.vibes.app.modules.messages.entities.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PrivateChatDeliveryStrategy implements MessageDeliveryStrategy {

    private final PrivateChatRepository privateChatRepository;

    public PrivateChatDeliveryStrategy(PrivateChatRepository privateChatRepository) {
        this.privateChatRepository = privateChatRepository;
    }

    @Override
    public boolean supports(String chatId) {
        try {
            return privateChatRepository.existsById(UUID.fromString(chatId));
        } catch (IllegalArgumentException e) {
            return false; // Invalid UUID format
        }
    }

    @Override
    public Object buildPayload(Message message, UUID senderId) {
        return MessageDTO.from(message);
    }

    @Override
    public List<UUID> resolveRecipients(String chatId, UUID senderId) {
        var chat = privateChatRepository.findById(UUID.fromString(chatId))
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        return List.of(chat.getUser1().getId(), chat.getUser2().getId());
    }

    @Override
    public void deliver(Object payload, List<UUID> recipients, SimpMessagingTemplate template) {
        for (UUID recipientId : recipients) {
            template.convertAndSendToUser(recipientId.toString(), "/queue/messages", payload);
        }
    }
}
