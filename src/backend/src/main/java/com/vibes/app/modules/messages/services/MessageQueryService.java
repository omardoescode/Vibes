package com.vibes.app.modules.messages.services;

import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.flyweight.MessageView;
import com.vibes.app.modules.messages.flyweight.UserProfileFlyweightFactory;
import com.vibes.app.modules.messages.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for retrieving message history with optional sender enrichment.
 * For group chats, messages are enriched with sender profile data using the flyweight pattern.
 */
@Service
public class MessageQueryService {

    private final MessageRepository messageRepository;
    private final GroupChatRepository groupChatRepository;
    private final UserProfileFlyweightFactory flyweightFactory;

    public MessageQueryService(MessageRepository messageRepository,
                               GroupChatRepository groupChatRepository,
                               UserProfileFlyweightFactory flyweightFactory) {
        this.messageRepository = messageRepository;
        this.groupChatRepository = groupChatRepository;
        this.flyweightFactory = flyweightFactory;
    }

    /**
     * Retrieves message history for a chat.
     * For group chats, returns MessageView objects with sender info.
     * For private chats, returns plain Message objects.
     */
    @Transactional(readOnly = true)
    public List<?> getMessagesForChat(String chatId) {
        UUID chatUuid = UUID.fromString(chatId);
        
        // Check if this is a group chat
        boolean isGroupChat = groupChatRepository.existsById(chatUuid);
        
        List<Message> messages = StreamSupport.stream(
                messageRepository.findByChatIdOrderByTimestampAsc(chatId).spliterator(), false)
                .collect(Collectors.toList());
        
        if (isGroupChat) {
            // Enrich with sender info for group chats
            return messages.stream()
                    .map(this::toMessageView)
                    .collect(Collectors.toList());
        } else {
            // Return plain messages for private chats
            return messages;
        }
    }
    
    private MessageView toMessageView(Message message) {
        UUID senderId = UUID.fromString(message.getSenderId());
        var flyweight = flyweightFactory.get(senderId);
        // For historical messages, we don't have real-time status, so use "offline"
        return new MessageView(message, flyweight, "offline");
    }
}
