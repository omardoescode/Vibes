package com.vibes.app.modules.search.operations;

import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.search.bridge.MessageSearchOperation;
import com.vibes.app.modules.search.bridge.SearchBackend;

import java.util.List;

/**
 * Refined Abstraction: Search across all user's chats globally.
 */
public class GlobalMessageSearch extends MessageSearchOperation {
    
    public GlobalMessageSearch(SearchBackend backend) {
        super(backend);
    }
    
    @Override
    public List<Message> search(String query, String userId) {
        // Search across all chats (backend will filter by user's accessible chats)
        return backend.search(query, null, userId);
    }
}