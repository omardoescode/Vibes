package com.vibes.app.modules.chat.mediator;

import com.vibes.app.modules.chat.group_chat.GroupChat;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.notifications.model.NotificationContext;
import com.vibes.app.modules.notifications.model.NotificationType;
import com.vibes.app.modules.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Component
public class GroupChatMediatorImpl implements GroupChatMediator {

    private final GroupChatRepository groupChatRepository;
    private final NotificationService notificationService;

    public GroupChatMediatorImpl(GroupChatRepository groupChatRepository,
                                 NotificationService notificationService) {
        this.groupChatRepository = groupChatRepository;
        this.notificationService = notificationService;
    }

    /**
     * Notifies all current group members — except the newly added user — that
     * someone joined the group.
     */
    @Override
    public void onMemberAdded(UUID groupId, UUID addedUserId) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        List<UUID> recipients = group.getMembers().stream()
                .map(member -> member.getId())
                .filter(id -> !id.equals(addedUserId))
                .toList();

        for (UUID recipientId : recipients) {
            notificationService.notifyMemberAdded(groupId, addedUserId, recipientId);
        }
    }

    /**
     * Notifies all remaining group members — except the removed user — that
     * someone left the group.
     */
    @Override
    public void onMemberRemoved(UUID groupId, UUID removedUserId) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        List<UUID> recipients = group.getMembers().stream()
                .map(member -> member.getId())
                .filter(id -> !id.equals(removedUserId))
                .toList();

        for (UUID recipientId : recipients) {
            notificationService.notifyMemberRemoved(groupId, removedUserId, recipientId);
        }
    }
}