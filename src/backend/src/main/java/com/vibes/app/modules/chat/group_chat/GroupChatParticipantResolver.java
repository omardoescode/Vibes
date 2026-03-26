package com.vibes.app.modules.chat.group_chat;

import com.vibes.app.modules.chat.ChatParticipantResolver;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GroupChatParticipantResolver implements ChatParticipantResolver {

    private final GroupChatRepository groupChatRepository;

    public GroupChatParticipantResolver(GroupChatRepository groupChatRepository) {
        this.groupChatRepository = groupChatRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> resolveRecipients(UUID chatId, UUID senderId) {
        return groupChatRepository.findById(chatId)
                .map(group -> group.getMembers().stream()
                        .filter(member -> !member.getId().equals(senderId))
                        .map(member -> member.getId())
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canResolve(UUID chatId) {
        return groupChatRepository.existsById(chatId);
    }
}
