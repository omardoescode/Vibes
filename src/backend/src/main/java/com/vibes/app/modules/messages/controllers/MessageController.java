package com.vibes.app.modules.messages.controllers;

import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("messagesMessagesController")
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
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