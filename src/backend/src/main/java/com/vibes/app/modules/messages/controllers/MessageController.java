package com.vibes.app.modules.messages.controllers;

import com.vibes.app.modules.chat.repositories.PrivateChatRepository;
import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController("messagesMessagesController")
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PrivateChatRepository chatRepository;

    @Autowired
    public MessageController(MessageService messageService,
                             SimpMessagingTemplate messagingTemplate,
                             PrivateChatRepository chatRepository) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.chatRepository = chatRepository;
    }

    @PostMapping("/text")
    public ResponseEntity<Message> sendTextMessage(
            @RequestParam("chatId") String chatId,
            @RequestParam("senderId") String senderId,
            @RequestParam("text") String text) {
        try {
            MessagePayload payload = new MessagePayload();
            payload.setChatId(chatId);
            payload.setSenderId(senderId);
            payload.setTextContent(text);

            Message savedMessage = messageService.processAndSaveTextMessage(payload);
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/media")
    public ResponseEntity<Message> sendMediaMessage(
            @RequestParam("chatId") String chatId,
            @RequestParam("senderId") String senderId,
            @RequestParam("file") MultipartFile file) {
        try {
            MessagePayload payload = new MessagePayload();
            payload.setChatId(chatId);
            payload.setSenderId(senderId);
            payload.setFileContent(file);

            Message savedMessage = messageService.processAndSaveMediaMessage(payload);

            // Broadcast to both chat participants over WebSocket so the receiver
            // sees the attachment instantly without needing to reload.
            var chat = chatRepository.findById(UUID.fromString(chatId));
            if (chat.isPresent()) {
                String user1Id = chat.get().getUser1().getId().toString();
                String user2Id = chat.get().getUser2().getId().toString();
                messagingTemplate.convertAndSendToUser(user1Id, "/queue/messages", savedMessage);
                messagingTemplate.convertAndSendToUser(user2Id, "/queue/messages", savedMessage);
            }

            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<Iterable<Message>> getChatMessages(@PathVariable String chatId) {
        return ResponseEntity.ok(messageService.getMessagesByChatId(chatId));
    }
}
