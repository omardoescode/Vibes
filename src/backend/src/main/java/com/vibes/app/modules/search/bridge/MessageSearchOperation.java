package com.vibes.app.modules.search.bridge;

import com.vibes.app.modules.messages.entities.Message;

import java.util.List;

/**
 * Abstraction for Bridge pattern.
 * Defines the search scope/operation.
 * Holds a reference to the SearchBackend (implementor).
 */
public abstract class MessageSearchOperation {
    protected final SearchBackend backend;
    
    public MessageSearchOperation(SearchBackend backend) {
        this.backend = backend;
    }
    
    /**
     * Execute the search operation.
     * 
     * @param query the search query
     * @param userId the user performing the search
     * @return list of matching messages
     */
    public abstract List<Message> search(String query, String userId);
    
    /**
     * Get the search backend being used.
     * 
     * @return the search backend
     */
    public SearchBackend getBackend() {
        return backend;
    }
}