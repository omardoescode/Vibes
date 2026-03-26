package com.vibes.app.modules.search.operations;

import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.search.bridge.MessageSearchOperation;
import com.vibes.app.modules.search.bridge.SearchBackend;

import java.util.List;

/**
 * Refined Abstraction: Search within a specific chat.
 */
public class ChatMessageSearch extends MessageSearchOperation {
    
    private final String chatId;
    
    public ChatMessageSearch(SearchBackend backend, String chatId) {
        super(backend);
        this.chatId = chatId;
    }
    
    @Override
    public List<Message> search(String query, String userId) {
        // In a real implementation, you'd verify the user has access to this chat
        return backend.search(query, chatId, userId);
    }
    
    public String getChatId() {
        return chatId;
    }
}