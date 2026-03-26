package com.vibes.app.modules.search.backends;

import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.repositories.MessageRepository;
import com.vibes.app.modules.search.bridge.SearchBackend;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Concrete Implementor: Uses PostgreSQL full-text search with tsvector.
 * More powerful than LIKE queries, supports stemming and ranking.
 * 
 * Uses PostgreSQL built-in full-text search capabilities (to_tsvector, to_tsquery).
 * These are core PostgreSQL functions and don't require additional extensions.
 */
@Component
public class FullTextSearchBackend implements SearchBackend {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final MessageRepository messageRepository;
    
    public FullTextSearchBackend(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    
    @Override
    public List<Message> search(String query, String chatId, String userId) {
        // Convert query to tsquery format (simple: words ANDed together)
        String tsQuery = query.trim().replaceAll("\\s+", " & ");
        
        String sql = "SELECT m.* FROM messages m " +
                     "WHERE to_tsvector('english', m.text_content) @@ to_tsquery('english', :query)";
        
        if (chatId != null) {
            sql += " AND m.chat_id = :chatId";
        }
        
        sql += " ORDER BY ts_rank(to_tsvector('english', m.text_content), to_tsquery('english', :query)) DESC";
        
        Query nativeQuery = entityManager.createNativeQuery(sql, Message.class);
        nativeQuery.setParameter("query", tsQuery);
        if (chatId != null) {
            nativeQuery.setParameter("chatId", chatId);
        }
        
        @SuppressWarnings("unchecked")
        List<Message> results = nativeQuery.getResultList();
        return results;
    }
    
    @Override
    public String getBackendName() {
        return "FULLTEXT";
    }
}