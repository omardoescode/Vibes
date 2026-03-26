package com.vibes.app.modules.search.backends;

import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.entities.TextMessage;
import com.vibes.app.modules.messages.repositories.MessageRepository;
import com.vibes.app.modules.search.bridge.SearchBackend;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Implementor: Uses database LIKE queries for searching.
 * Simple but effective for basic search needs.
 */
@Component
public class DatabaseSearchBackend implements SearchBackend {
    
    private final MessageRepository messageRepository;
    
    public DatabaseSearchBackend(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    
    @Override
    public List<Message> search(String query, String chatId, String userId) {
        // Get all messages (in a real implementation, you'd filter by user's accessible chats)
        List<Message> allMessages;
        if (chatId != null) {
            allMessages = messageRepository.findByChatIdOrderByTimestampAsc(chatId);
        } else {
            allMessages = messageRepository.findAll();
        }
        
        // Filter using case-insensitive LIKE pattern matching on content
        String lowerQuery = query.toLowerCase();
        return allMessages.stream()
            .filter(msg -> {
                String content = msg.getContent();
                return content != null && content.toLowerCase().contains(lowerQuery);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public String getBackendName() {
        return "DATABASE";
    }
}