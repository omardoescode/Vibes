package com.vibes.app.modules.search.bridge;

import com.vibes.app.modules.messages.entities.Message;

import java.util.List;

/**
 * Implementor interface for Bridge pattern.
 * Defines how to perform the actual search.
 */
public interface SearchBackend {
    /**
     * Search for messages matching the query.
     * 
     * @param query the search query
     * @param chatId optional chat ID to restrict search (null for global search)
     * @param userId the user performing the search (for permission checks)
     * @return list of matching messages
     */
    List<Message> search(String query, String chatId, String userId);
    
    /**
     * Get the name of this search backend.
     * 
     * @return backend name
     */
    String getBackendName();
}