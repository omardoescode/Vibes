package com.vibes.app.modules.search.backends;

import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.repositories.MessageRepository;
import com.vibes.app.modules.search.bridge.SearchBackend;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Concrete Implementor: Uses PostgreSQL fuzzy string matching (pg_trgm).
 * Supports similarity search using Levenshtein distance.
 * 
 * Requires PostgreSQL with the pg_trgm extension enabled.
 * This is automatically done via data.sql on startup.
 */
@Component
public class FuzzySearchBackend implements SearchBackend {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final MessageRepository messageRepository;
    private final float similarityThreshold;
    
    @Autowired
    public FuzzySearchBackend(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        this.similarityThreshold = 0.3f; // Default 30% similarity
    }
    
    @Override
    public List<Message> search(String query, String chatId, String userId) {
        // Set similarity threshold for this session
        // Use CAST to ensure proper type matching with PostgreSQL real type
        String setThreshold = "SELECT set_limit(CAST(:threshold AS real))";
        Query thresholdQuery = entityManager.createNativeQuery(setThreshold);
        thresholdQuery.setParameter("threshold", similarityThreshold);
        thresholdQuery.getSingleResult();
        
        // Perform fuzzy search using % operator
        String sql = "SELECT m.* FROM messages m " +
                     "WHERE m.text_content % :query";
        
        if (chatId != null) {
            sql += " AND m.chat_id = :chatId";
        }
        
        sql += " ORDER BY similarity(m.text_content, :query) DESC";
        
        Query nativeQuery = entityManager.createNativeQuery(sql, Message.class);
        nativeQuery.setParameter("query", query);
        if (chatId != null) {
            nativeQuery.setParameter("chatId", chatId);
        }
        
        @SuppressWarnings("unchecked")
        List<Message> results = nativeQuery.getResultList();
        return results;
    }
    
    @Override
    public String getBackendName() {
        return "FUZZY";
    }
    
    public float getSimilarityThreshold() {
        return similarityThreshold;
    }
}