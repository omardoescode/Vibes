package com.vibes.app.modules.export.operations;

import com.vibes.app.modules.export.bridge.ExportFormatter;
import com.vibes.app.modules.export.bridge.ExportOperation;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.repositories.MessageRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Refined Abstraction: Exports messages from specific sender(s).
 */
public class SenderFilteredExport extends ExportOperation {
    
    private final String chatId;
    private final Set<String> senderIds;
    private final MessageRepository messageRepository;
    
    public SenderFilteredExport(ExportFormatter formatter, String chatId,
                               Set<String> senderIds,
                               MessageRepository messageRepository) {
        super(formatter);
        this.chatId = chatId;
        this.senderIds = senderIds;
        this.messageRepository = messageRepository;
    }
    
    @Override
    public String execute() {
        List<Message> messages = getMessages();
        return formatter.format(messages);
    }
    
    @Override
    protected List<Message> getMessages() {
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId).stream()
            .filter(msg -> senderIds.contains(msg.getSenderId()))
            .collect(Collectors.toList());
    }
    
    public String getChatId() {
        return chatId;
    }
    
    public Set<String> getSenderIds() {
        return senderIds;
    }
}