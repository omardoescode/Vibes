package com.vibes.app.modules.chat.mediator;

import com.vibes.app.modules.chat.group_chat.GroupChat;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GroupCreationMediatorImpl implements GroupCreationMediator {

    private final GroupChatRepository groupChatRepository;
    private final NotificationService notificationService;

    public GroupCreationMediatorImpl(GroupChatRepository groupChatRepository,
                                     NotificationService notificationService) {
        this.groupChatRepository = groupChatRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void onGroupCreated(UUID groupId) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        UUID creatorId = group.getCreator().getId();

        List<UUID> recipients = group.getMembers().stream()
                .map(member -> member.getId())
                .filter(id -> !id.equals(creatorId))
                .toList();

        for (UUID recipientId : recipients) {
            notificationService.notifyGroupCreated(groupId, creatorId, recipientId);
        }
    }
}
