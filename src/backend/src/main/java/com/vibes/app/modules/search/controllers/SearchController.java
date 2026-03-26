package com.vibes.app.modules.search.controllers;

import com.vibes.app.modules.messages.dto.MessageDTO;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.search.bridge.SearchBackend;
import com.vibes.app.modules.search.backends.FullTextSearchBackend;
import com.vibes.app.modules.search.backends.FuzzySearchBackend;
import com.vibes.app.modules.search.operations.ChatMessageSearch;
import com.vibes.app.modules.search.operations.GlobalMessageSearch;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for message search functionality.
 * 
 * Endpoints:
 * - GET /chats/{chatId}/messages/search?query=hello&fuzzy=false
 * - GET /messages/search?query=hello&fuzzy=false
 */
@RestController
public class SearchController {

    private final FullTextSearchBackend fullTextBackend;
    private final FuzzySearchBackend fuzzyBackend;

    public SearchController(FullTextSearchBackend fullTextBackend,
                           FuzzySearchBackend fuzzyBackend) {
        this.fullTextBackend = fullTextBackend;
        this.fuzzyBackend = fuzzyBackend;
    }

    /**
     * Search messages within a specific chat.
     * Uses full-text search by default, or fuzzy search if fuzzy=true.
     * GET /chats/{chatId}/messages/search?query=hello&fuzzy=false
     */
    @GetMapping("/chats/{chatId}/messages/search")
    public ResponseEntity<List<MessageDTO>> searchChatMessages(
            @PathVariable String chatId,
            @RequestParam String query,
            @RequestParam(defaultValue = "false") boolean fuzzy,
            Authentication authentication) {
        
        String userId = getUserIdFromAuthentication(authentication);
        SearchBackend searchBackend = fuzzy ? fuzzyBackend : fullTextBackend;
        ChatMessageSearch searchOperation = new ChatMessageSearch(searchBackend, chatId);
        List<Message> results = searchOperation.search(query, userId);
        
        return ResponseEntity.ok(results.stream()
                .map(MessageDTO::from)
                .collect(Collectors.toList()));
    }

    /**
     * Search messages globally across all user's chats.
     * Uses full-text search by default, or fuzzy search if fuzzy=true.
     * GET /messages/search?query=hello&fuzzy=false
     */
    @GetMapping("/messages/search")
    public ResponseEntity<List<MessageDTO>> searchGlobalMessages(
            @RequestParam String query,
            @RequestParam(defaultValue = "false") boolean fuzzy,
            Authentication authentication) {
        
        String userId = getUserIdFromAuthentication(authentication);
        SearchBackend searchBackend = fuzzy ? fuzzyBackend : fullTextBackend;
        GlobalMessageSearch searchOperation = new GlobalMessageSearch(searchBackend);
        List<Message> results = searchOperation.search(query, userId);
        
        return ResponseEntity.ok(results.stream()
                .map(MessageDTO::from)
                .collect(Collectors.toList()));
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }
}