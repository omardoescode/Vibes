package com.vibes.app.modules.websocket;

import com.vibes.app.modules.auth.services.AuthService;
import com.vibes.app.modules.notifications.presence.UserPresenceManager;
import com.vibes.app.modules.chat.ChatParticipantResolver;
import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.notifications.service.NotificationService;
import com.vibes.app.modules.websocket.handlers.MediaMessageHandler;
import com.vibes.app.modules.websocket.handlers.TextMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class ChatWebSocketController {

  private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

  private final ChatSessionRegistry sessionRegistry;
  private final AuthService authService;
  private final UserPresenceManager presenceManager;
  private final NotificationService notificationService;
  private final ChatParticipantResolver participantResolver;
  private final TextMessageHandler textMessageHandler;
  private final MediaMessageHandler mediaMessageHandler;

  public ChatWebSocketController(ChatSessionRegistry sessionRegistry,
      AuthService authService,
      UserPresenceManager presenceManager,
      NotificationService notificationService,
      ChatParticipantResolver participantResolver,
      TextMessageHandler textMessageHandler,
      MediaMessageHandler mediaMessageHandler) {
    this.sessionRegistry = sessionRegistry;
    this.authService = authService;
    this.presenceManager = presenceManager;
    this.notificationService = notificationService;
    this.participantResolver = participantResolver;
    this.textMessageHandler = textMessageHandler;
    this.mediaMessageHandler = mediaMessageHandler;
  }

  // -------------------------------------------------------------------------
  // Chat presence
  // -------------------------------------------------------------------------

  @MessageMapping("/chat.open")
  public void openChat(@Payload Map<String, String> payload, Principal principal) {
    String userId = principal.getName();
    String chatId = payload.get("chatId");
    if (chatId != null) {
      sessionRegistry.openChat(userId, chatId);
      log.debug("[chat.open] user={} chat={}", userId, chatId);
    }
  }

  @MessageMapping("/chat.close")
  public void closeChat(Principal principal) {
    String userId = principal.getName();
    sessionRegistry.closeChat(userId);
    log.debug("[chat.close] user={}", userId);
  }

  @EventListener
  public void onConnect(SessionConnectedEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    Principal principal = accessor.getUser();
    if (principal != null) {
      String userId = principal.getName();
      try {
        presenceManager.setOnline(UUID.fromString(userId));
        log.debug("[ws.connect] user={} marked online", userId);
      } catch (Exception e) {
        log.warn("[ws.connect] failed to mark user={} online: {}", userId, e.getMessage());
      }
    }
  }

  @EventListener
  public void onDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    Principal principal = accessor.getUser();
    if (principal != null) {
      String userId = principal.getName();
      log.debug("[ws.disconnect] user={} disconnected", userId);
      try {
        presenceManager.setOffline(UUID.fromString(userId));
        log.debug("[ws.disconnect] user={} marked offline", userId);
      } catch (Exception e) {
        log.warn("[ws.disconnect] failed to mark user={} offline: {}", userId, e.getMessage());
      }
    }
  }

  // -------------------------------------------------------------------------
  // Private chat messaging
  // -------------------------------------------------------------------------

  @MessageMapping("/chat.send")
  public void sendMessage(@Payload MessagePayload payload, Principal principal) throws Exception {
    textMessageHandler.handle(payload, principal);
  }

  @MessageMapping("/chat.media")
  public void sendMediaMessage(@Payload MessagePayload payload, Principal principal) throws Exception {
    mediaMessageHandler.handle(payload, principal);
  }

  @MessageMapping("/chat.typing")
  public void typing(@Payload Map<String, Object> payload, Principal principal) {
    UUID senderId = UUID.fromString(principal.getName());
    UUID chatId = UUID.fromString((String) payload.get("chatId"));
    Boolean isTyping = (Boolean) payload.get("isTyping");

    List<UUID> recipients = participantResolver.resolveRecipients(chatId, senderId);
    recipients.forEach(recipientId ->
        notificationService.notifyTyping(chatId, senderId, recipientId, isTyping != null && isTyping));
  }

  // -------------------------------------------------------------------------
  // Group chat messaging
  // -------------------------------------------------------------------------

  @MessageMapping("/group.send")
  public void sendGroupMessage(@Payload MessagePayload payload, Principal principal) throws Exception {
    textMessageHandler.handle(payload, principal);
  }

  @MessageMapping("/group.media")
  public void sendGroupMediaMessage(@Payload MessagePayload payload, Principal principal) throws Exception {
    mediaMessageHandler.handle(payload, principal);
  }
}
