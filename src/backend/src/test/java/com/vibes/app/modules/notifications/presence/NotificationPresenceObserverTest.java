package com.vibes.app.modules.notifications.presence;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import com.vibes.app.modules.chat.group_chat.GroupChat;
import com.vibes.app.modules.chat.private_chat.PrivateChat;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.chat.repositories.PrivateChatRepository;
import com.vibes.app.modules.notifications.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPresenceObserverTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PrivateChatRepository privateChatRepository;

    @Mock
    private GroupChatRepository groupChatRepository;

    private NotificationPresenceObserver observer;

    @BeforeEach
    void setUp() {
        observer = new NotificationPresenceObserver(notificationService, userRepository, privateChatRepository, groupChatRepository);
    }

    @Test
    void onPresenceChanged_offline_notifiesPrivateChatPartner() {
        UUID userId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();

        User user = mock(User.class);
        User partner = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(partner.getId()).thenReturn(partnerId);

        PrivateChat chat = mock(PrivateChat.class);
        when(chat.getUser1()).thenReturn(user);
        when(chat.getUser2()).thenReturn(partner);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(privateChatRepository.findAllByUser(user)).thenReturn(List.of(chat));
        when(groupChatRepository.findAllByMember(user)).thenReturn(List.of());

        observer.onPresenceChanged(userId, "testuser", false);

        verify(notificationService).notifyUserStatus(partnerId, userId, "testuser", false);
    }

    @Test
    void onPresenceChanged_online_notifiesGroupChatMembers() {
        UUID userId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        User user = mock(User.class);
        User member = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(member.getId()).thenReturn(memberId);

        GroupChat group = mock(GroupChat.class);
        when(group.getMembers()).thenReturn(List.of(user, member));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(privateChatRepository.findAllByUser(user)).thenReturn(List.of());
        when(groupChatRepository.findAllByMember(user)).thenReturn(List.of(group));

        observer.onPresenceChanged(userId, "testuser", true);

        verify(notificationService).notifyUserStatus(memberId, userId, "testuser", true);
        verify(notificationService, never()).notifyUserStatus(eq(userId), any(), any(), anyBoolean());
    }

    @Test
    void onPresenceChanged_noChats_noNotificationsSent() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(privateChatRepository.findAllByUser(user)).thenReturn(List.of());
        when(groupChatRepository.findAllByMember(user)).thenReturn(List.of());

        observer.onPresenceChanged(userId, "testuser", false);

        verifyNoInteractions(notificationService);
    }

    @Test
    void onPresenceChanged_userNotFound_doesNothing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        observer.onPresenceChanged(userId, "ghost", false);

        verifyNoInteractions(notificationService, privateChatRepository, groupChatRepository);
    }
}
