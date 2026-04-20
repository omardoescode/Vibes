package com.vibes.app.modules.websocket.delivery;

import com.vibes.app.modules.chat.group_chat.GroupChat;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.flyweight.MessageView;
import com.vibes.app.modules.messages.flyweight.UserProfileFlyweight;
import com.vibes.app.modules.messages.flyweight.UserProfileFlyweightFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GroupChatDeliveryStrategy implements MessageDeliveryStrategy {

    private final GroupChatRepository groupChatRepository;
    private final UserProfileFlyweightFactory flyweightFactory;

    public GroupChatDeliveryStrategy(GroupChatRepository groupChatRepository, UserProfileFlyweightFactory flyweightFactory) {
        this.groupChatRepository = groupChatRepository;
        this.flyweightFactory = flyweightFactory;
    }

    @Override
    public boolean supports(String chatId) {
        try {
            return groupChatRepository.existsById(UUID.fromString(chatId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Object buildPayload(Message message, UUID senderId) {
        GroupChat group = groupChatRepository.findById(UUID.fromString(message.getChatId().toString()))
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        UserProfileFlyweight flyweight = flyweightFactory.get(senderId);
        String senderStatus = group.getMembers().stream()
                .filter(m -> m.getId().equals(senderId))
                .findFirst()
                .map(m -> m.getStatus())
                .orElse("offline");

        return new MessageView(message, flyweight, senderStatus);
    }

    @Override
    public List<UUID> resolveRecipients(String chatId, UUID senderId) {
        GroupChat group = groupChatRepository.findById(UUID.fromString(chatId))
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return group.getMembers().stream()
                .map(member -> member.getId())
                .collect(Collectors.toList());
    }

    @Override
    public void deliver(Object payload, List<UUID> recipients, SimpMessagingTemplate template) {
        for (UUID recipientId : recipients) {
            template.convertAndSendToUser(recipientId.toString(), "/queue/messages", payload);
        }
    }
}
