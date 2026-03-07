package com.vibes.app.modules.chat.controllers;

import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import com.vibes.app.modules.auth.repositories.UserRepository;
import com.vibes.app.modules.chat.dto.ChatResponse;
import com.vibes.app.modules.chat.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final UserCredentialsRepository userCredentialsRepository;

    public ChatController(ChatService chatService,
                          UserRepository userRepository,
                          UserCredentialsRepository userCredentialsRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
        this.userCredentialsRepository = userCredentialsRepository;
    }

    /**
     * Start or open a chat with another user by their UUID.
     * Returns the chat details (including chatId to connect via WebSocket).
     * POST /chats/start?targetUserId={uuid}
     */
    @PostMapping("/start")
    public ResponseEntity<?> startChat(@RequestParam UUID targetUserId, Authentication authentication) {
        try {
            UUID currentUserId = resolveUserId(authentication);
            ChatResponse response = chatService.getOrCreateChat(currentUserId, targetUserId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all chats for the authenticated user.
     * GET /chats
     */
    @GetMapping
    public ResponseEntity<List<ChatResponse>> listChats(Authentication authentication) {
        UUID currentUserId = resolveUserId(authentication);
        return ResponseEntity.ok(chatService.getChatsForUser(currentUserId));
    }

    private UUID resolveUserId(Authentication authentication) {
        String email = authentication.getName();
        var credentials = userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return credentials.getUser().getId();
    }
}
