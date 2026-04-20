package com.vibes.app.modules.notifications.presence;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import com.vibes.app.modules.chat.private_chat.PrivateChat;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.chat.repositories.PrivateChatRepository;
import com.vibes.app.modules.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class NotificationPresenceObserver implements PresenceObserver {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final PrivateChatRepository privateChatRepository;
    private final GroupChatRepository groupChatRepository;

    public NotificationPresenceObserver(NotificationService notificationService,
                                        UserRepository userRepository,
                                        PrivateChatRepository privateChatRepository,
                                        GroupChatRepository groupChatRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.privateChatRepository = privateChatRepository;
        this.groupChatRepository = groupChatRepository;
    }

    @Override
    public void onPresenceChanged(UUID userId, String username, boolean isOnline) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Set<UUID> partners = resolveChatPartners(user);
        for (UUID partnerId : partners) {
            notificationService.notifyUserStatus(partnerId, userId, username, isOnline);
        }
    }

    private Set<UUID> resolveChatPartners(User user) {
        Set<UUID> partners = new HashSet<>();

        List<PrivateChat> privateChats = privateChatRepository.findAllByUser(user);
        for (PrivateChat chat : privateChats) {
            UUID partnerId = chat.getUser1().getId().equals(user.getId())
                    ? chat.getUser2().getId()
                    : chat.getUser1().getId();
            partners.add(partnerId);
        }

        groupChatRepository.findAllByMember(user).forEach(group ->
                group.getMembers().stream()
                        .map(User::getId)
                        .filter(id -> !id.equals(user.getId()))
                        .forEach(partners::add)
        );

        return partners;
    }
}
