package com.vibes.app.modules.websocket;

import com.vibes.app.modules.auth.services.AuthService;
import com.vibes.app.modules.notifications.presence.UserPresenceManager;
import com.vibes.app.modules.chat.ChatParticipantResolver;
import com.vibes.app.modules.chat.group_chat.GroupChat;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.chat.repositories.PrivateChatRepository;
import com.vibes.app.modules.messages.dto.MessageDTO;
import com.vibes.app.modules.messages.dto.MessagePayload;
import org.springframework.transaction.annotation.Transactional;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.flyweight.MessageView;
import com.vibes.app.modules.messages.flyweight.UserProfileFlyweight;
import com.vibes.app.modules.messages.flyweight.UserProfileFlyweightFactory;
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
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class ChatWebSocketController {

  private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

  private final MessageService messageService;
  private final SimpMessagingTemplate messagingTemplate;
  private final PrivateChatRepository privateChatRepository;
  private final GroupChatRepository groupChatRepository;
  private final ChatSessionRegistry sessionRegistry;
  private final AuthService authService;
  private final UserPresenceManager presenceManager;
  private final NotificationService notificationService;
  private final UserProfileFlyweightFactory flyweightFactory;
  private final ChatParticipantResolver participantResolver;

  public ChatWebSocketController(MessageService messageService,
      SimpMessagingTemplate messagingTemplate,
      PrivateChatRepository privateChatRepository,
      GroupChatRepository groupChatRepository,
      ChatSessionRegistry sessionRegistry,
      AuthService authService,
      UserPresenceManager presenceManager,
      NotificationService notificationService,
      UserProfileFlyweightFactory flyweightFactory,
      ChatParticipantResolver participantResolver) {
    this.messageService = messageService;
    this.messagingTemplate = messagingTemplate;
    this.privateChatRepository = privateChatRepository;
    this.groupChatRepository = groupChatRepository;
    this.sessionRegistry = sessionRegistry;
    this.authService = authService;
    this.presenceManager = presenceManager;
    this.notificationService = notificationService;
    this.flyweightFactory = flyweightFactory;
    this.participantResolver = participantResolver;
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
   */
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

  /**
   * Clean up registry when a WebSocket session is terminated.
   */
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
    var chat = privateChatRepository.findById(chatId)
        .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

    String user1Id = chat.getUser1().getId().toString();
    String user2Id = chat.getUser2().getId().toString();

    // Use abstraction: normalize to DTO for consistent frontend format
    MessageDTO messageDTO = MessageDTO.from(saved);
    messagingTemplate.convertAndSendToUser(user1Id, "/queue/messages", messageDTO);
    messagingTemplate.convertAndSendToUser(user2Id, "/queue/messages", messageDTO);

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
    var chat = privateChatRepository.findById(chatId)
        .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

    String user1Id = chat.getUser1().getId().toString();
    String user2Id = chat.getUser2().getId().toString();

    // Use abstraction: normalize to DTO for consistent frontend format
    MessageDTO messageDTO = MessageDTO.from(saved);
    messagingTemplate.convertAndSendToUser(user1Id, "/queue/messages", messageDTO);
    messagingTemplate.convertAndSendToUser(user2Id, "/queue/messages", messageDTO);

    // Send notification to the recipient (the other user, not the sender)
    UUID recipientId = chat.getUser1().getId().equals(senderId)
        ? chat.getUser2().getId()
        : chat.getUser1().getId();

    System.out.println(
        "[ChatWebSocketController.media] Sending notification to recipient: " + recipientId + ", sender: " + senderId);
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

    // Use abstraction to resolve recipients - works for both private and group chats
    List<UUID> recipients = participantResolver.resolveRecipients(chatId, senderId);
    recipients.forEach(recipientId -> 
        notificationService.notifyTyping(chatId, senderId, recipientId, isTyping != null && isTyping));
  }

  // -------------------------------------------------------------------------
  // Group chat messaging
  // -------------------------------------------------------------------------

  /**
   * Client sends to /app/group.send
   * Payload: { chatId, textContent }
   *
   * Saves the message then broadcasts a MessageView (with flyweight sender info)
   * to every group member individually via their personal queue.
   */
  @MessageMapping("/group.send")
  @Transactional
  public void sendGroupMessage(@Payload MessagePayload payload, Principal principal) throws Exception {
    UUID senderId = UUID.fromString(principal.getName());
    payload.setSenderId(senderId.toString());

    Message saved = messageService.processAndSaveTextMessage(payload);

    UUID chatId = UUID.fromString(payload.getChatId());
    GroupChat group = groupChatRepository.findById(chatId)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));

    // Build MessageView using flyweight — sender profile is cached after first
    // lookup
    UserProfileFlyweight flyweight = flyweightFactory.get(senderId);
    String senderStatus = group.getMembers().stream()
        .filter(m -> m.getId().equals(senderId))
        .findFirst()
        .map(m -> m.getStatus())
        .orElse("offline");

    MessageView messageView = new MessageView(saved, flyweight, senderStatus);

    // Deliver to every member's personal queue
    group.getMembers().forEach(member -> messagingTemplate.convertAndSendToUser(
        member.getId().toString(), "/queue/messages", messageView));

    // Send notifications to all members except sender
    group.getMembers().stream()
        .filter(member -> !member.getId().equals(senderId))
        .forEach(member -> notificationService.notifyNewMessage(saved, member.getId()));

    log.debug("[group.send] chatId={} sender={} members={}", chatId, senderId, group.getMembers().size());
  }

  /**
   * Client sends to /app/group.media
   * Payload: { chatId, fileContent }
   */
  @MessageMapping("/group.media")
  @Transactional
  public void sendGroupMediaMessage(@Payload MessagePayload payload, Principal principal) throws Exception {
    UUID senderId = UUID.fromString(principal.getName());
    payload.setSenderId(senderId.toString());

    Message saved = messageService.processAndSaveMediaMessage(payload);

    UUID chatId = UUID.fromString(payload.getChatId());
    GroupChat group = groupChatRepository.findById(chatId)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));

    // Build MessageView using flyweight — sender profile is cached after first
    // lookup
    UserProfileFlyweight flyweight = flyweightFactory.get(senderId);
    String senderStatus = group.getMembers().stream()
        .filter(m -> m.getId().equals(senderId))
        .findFirst()
        .map(m -> m.getStatus())
        .orElse("offline");

    MessageView messageView = new MessageView(saved, flyweight, senderStatus);

    group.getMembers().forEach(member -> messagingTemplate.convertAndSendToUser(
        member.getId().toString(), "/queue/messages", messageView));

    // Send notifications to all members except sender
    group.getMembers().stream()
        .filter(member -> !member.getId().equals(senderId))
        .forEach(member -> notificationService.notifyNewMessage(saved, member.getId()));

    log.debug("[group.media] chatId={} sender={} members={}", chatId, senderId, group.getMembers().size());
  }
}
