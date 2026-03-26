package com.vibes.app.modules.websocket;

import com.vibes.app.modules.auth.services.AuthService;
import com.vibes.app.modules.chat.repositories.PrivateChatRepository;
import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.services.MessageService;
import com.vibes.app.modules.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PrivateChatRepository chatRepository;
    private final ChatSessionRegistry sessionRegistry;
    private final AuthService authService;
    private final NotificationService notificationService;

    public ChatWebSocketController(MessageService messageService,
                                   SimpMessagingTemplate messagingTemplate,
                                   PrivateChatRepository chatRepository,
                                   ChatSessionRegistry sessionRegistry,
                                   AuthService authService,
                                   NotificationService notificationService) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.chatRepository = chatRepository;
        this.sessionRegistry = sessionRegistry;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // Chat presence
    // -------------------------------------------------------------------------

    /**
     * Client sends to /app/chat.open when it navigates into a chat.
     * Payload: { "chatId": "<uuid>" }
     */
    @MessageMapping("/chat.open")
    public void openChat(@Payload Map<String, String> payload, Principal principal) {
        String userId = principal.getName();
        String chatId = payload.get("chatId");
        if (chatId != null) {
            sessionRegistry.openChat(userId, chatId);
            log.debug("[chat.open] user={} chat={}", userId, chatId);
        }
    }

    /**
     * Client sends to /app/chat.close when it navigates away from a chat.
     * Payload: ignored.
     */
    @MessageMapping("/chat.close")
    public void closeChat(Principal principal) {
        String userId = principal.getName();
        sessionRegistry.closeChat(userId);
        log.debug("[chat.close] user={}", userId);
    }

    /**
     * Clean up registry when a WebSocket session is terminated (tab closed, network drop, etc.)
     * Also marks the user as offline in the database.
     */
    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal != null) {
            String userId = principal.getName();
            sessionRegistry.closeChat(userId);
            log.debug("[ws.disconnect] user={} removed from registry", userId);
            try {
                authService.updateStatus(UUID.fromString(userId), "offline");
                log.debug("[ws.disconnect] user={} marked offline", userId);
            } catch (Exception e) {
                log.warn("[ws.disconnect] failed to mark user={} offline: {}", userId, e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Messaging
    // -------------------------------------------------------------------------

    /**
     * Client sends to /app/chat.send
     * Payload: { chatId, textContent }
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessagePayload payload, Principal principal) throws Exception {
        UUID senderId = UUID.fromString(principal.getName());
        payload.setSenderId(senderId.toString());

        Message saved = messageService.processAndSaveTextMessage(payload);

        UUID chatId = UUID.fromString(payload.getChatId());
        var chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        String user1Id = chat.getUser1().getId().toString();
        String user2Id = chat.getUser2().getId().toString();

        messagingTemplate.convertAndSendToUser(user1Id, "/queue/messages", saved);
        messagingTemplate.convertAndSendToUser(user2Id, "/queue/messages", saved);

        // Send notification to the recipient (the other user, not the sender)
        UUID recipientId = chat.getUser1().getId().equals(senderId) 
            ? chat.getUser2().getId() 
            : chat.getUser1().getId();
        notificationService.notifyNewMessage(saved, recipientId);
    }

    /**
     * Client sends to /app/chat.media
     */
    @MessageMapping("/chat.media")
    public void sendMediaMessage(@Payload MessagePayload payload, Principal principal) throws Exception {
        UUID senderId = UUID.fromString(principal.getName());
        payload.setSenderId(senderId.toString());

        Message saved = messageService.processAndSaveMediaMessage(payload);

        UUID chatId = UUID.fromString(payload.getChatId());
        var chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        String user1Id = chat.getUser1().getId().toString();
        String user2Id = chat.getUser2().getId().toString();

        messagingTemplate.convertAndSendToUser(user1Id, "/queue/messages", saved);
        messagingTemplate.convertAndSendToUser(user2Id, "/queue/messages", saved);

        // Send notification to the recipient (the other user, not the sender)
        UUID recipientId = chat.getUser1().getId().equals(senderId) 
            ? chat.getUser2().getId() 
            : chat.getUser1().getId();
        
        System.out.println("[ChatWebSocketController.media] Sending notification to recipient: " + recipientId + ", sender: " + senderId);
        notificationService.notifyNewMessage(saved, recipientId);
    }

    /**
     * Client sends to /app/chat.typing when user starts/stops typing
     * Payload: { chatId, isTyping: true/false }
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, Object> payload, Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());
        UUID chatId = UUID.fromString((String) payload.get("chatId"));
        Boolean isTyping = (Boolean) payload.get("isTyping");

        var chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        // Send typing indicator to the other user (not the sender)
        UUID recipientId = chat.getUser1().getId().equals(senderId) 
            ? chat.getUser2().getId() 
            : chat.getUser1().getId();
        
        notificationService.notifyTyping(chatId, senderId, recipientId, isTyping != null && isTyping);
    }
}
