package com.vibes.app.modules.messages.factory;

import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.Message;

public abstract class MessageFactory {
    public abstract Message createMessage(MessagePayload payload) throws Exception;
}