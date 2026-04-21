package com.vibes.app.modules.export.operations;

import com.vibes.app.modules.export.bridge.ExportFormatter;
import com.vibes.app.modules.export.bridge.ExportOperation;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.repositories.MessageRepository;

import java.util.List;

public class FullChatExport extends ExportOperation {
    
    private final String chatId;
    private final MessageRepository messageRepository;
    
    public FullChatExport(ExportFormatter formatter, String chatId, MessageRepository messageRepository) {
        super(formatter);
        this.chatId = chatId;
        this.messageRepository = messageRepository;
    }
    
    @Override
    protected List<Message> getMessages() {
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId);
    }
    
    public String getChatId() {
        return chatId;
    }
}