package com.vibes.app.modules.messages.factory;

import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.entities.TextMessage;
import org.springframework.stereotype.Component;

@Component
public class TextMessageFactory extends MessageFactory {
    @Override
    public Message createMessage(MessagePayload payload) {
        if (payload.getTextContent() == null || payload.getTextContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Text content cannot be null");
        }
        return new TextMessage(payload.getTextContent(), payload.getSenderId(), payload.getChatId());
    }
}