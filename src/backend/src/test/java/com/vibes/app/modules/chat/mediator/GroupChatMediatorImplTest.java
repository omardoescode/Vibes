package com.vibes.app.modules.chat.mediator;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.chat.group_chat.GroupChat;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.notifications.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupChatMediatorImplTest {

    @Mock
    private GroupChatRepository groupChatRepository;

    @Mock
    private NotificationService notificationService;

    private GroupChatMediatorImpl mediator;

    private final UUID groupId   = UUID.randomUUID();
    private final UUID creatorId = UUID.randomUUID();
    private final UUID memberId1 = UUID.randomUUID();
    private final UUID memberId2 = UUID.randomUUID();
    private final UUID newMember = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mediator = new GroupChatMediatorImpl(groupChatRepository, notificationService);
    }

    // -------------------------------------------------------------------------
    // onMemberAdded
    // -------------------------------------------------------------------------

    @Test
    void onMemberAdded_notifiesAllMembersExceptAddedUser() {
        // Group already contains the new member (post-save state):
        // creator + memberId1 + memberId2 + newMember
        GroupChat group = mockedGroupWithMemberIds(creatorId, memberId1, memberId2, newMember);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));

        mediator.onMemberAdded(groupId, newMember);

        ArgumentCaptor<UUID> recipientCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(notificationService, times(3))
                .notifyMemberAdded(eq(groupId), eq(newMember), recipientCaptor.capture());

        assertThat(recipientCaptor.getAllValues())
                .containsExactlyInAnyOrder(creatorId, memberId1, memberId2)
                .doesNotContain(newMember);
    }

    @Test
    void onMemberAdded_singleRemainingMember_sendsExactlyOneNotification() {
        // Only creator + newMember in the group
        GroupChat group = mockedGroupWithMemberIds(creatorId, newMember);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));

        mediator.onMemberAdded(groupId, newMember);

        verify(notificationService, times(1))
                .notifyMemberAdded(groupId, newMember, creatorId);
    }

    @Test
    void onMemberAdded_groupNotFound_throwsIllegalArgumentException() {
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediator.onMemberAdded(groupId, newMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(groupId.toString());

        verifyNoInteractions(notificationService);
    }

    // -------------------------------------------------------------------------
    // onMemberRemoved
    // -------------------------------------------------------------------------

    @Test
    void onMemberRemoved_notifiesAllRemainingMembersExceptRemovedUser() {
        // Post-save state: removedUser is already gone, only creator + memberId1 remain
        UUID removedUser = memberId2;
        GroupChat group = mockedGroupWithMemberIds(creatorId, memberId1);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));

        mediator.onMemberRemoved(groupId, removedUser);

        ArgumentCaptor<UUID> recipientCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(notificationService, times(2))
                .notifyMemberRemoved(eq(groupId), eq(removedUser), recipientCaptor.capture());

        assertThat(recipientCaptor.getAllValues())
                .containsExactlyInAnyOrder(creatorId, memberId1)
                .doesNotContain(removedUser);
    }

    @Test
    void onMemberRemoved_onlyCreatorLeft_sendsExactlyOneNotification() {
        UUID removedUser = memberId1;
        GroupChat group = mockedGroupWithMemberIds(creatorId);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));

        mediator.onMemberRemoved(groupId, removedUser);

        verify(notificationService, times(1))
                .notifyMemberRemoved(groupId, removedUser, creatorId);
    }

    @Test
    void onMemberRemoved_groupNotFound_throwsIllegalArgumentException() {
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediator.onMemberRemoved(groupId, memberId1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(groupId.toString());

        verifyNoInteractions(notificationService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns a mocked GroupChat whose getMembers() returns User mocks with the
     * given IDs. Bypasses the real GroupChat constructor (which deduplicates the
     * creator) so the test controls the exact member list.
     */
    private GroupChat mockedGroupWithMemberIds(UUID... ids) {
        List<User> members = Arrays.stream(ids)
                .map(this::userWithId)
                .collect(Collectors.toList());
        GroupChat group = mock(GroupChat.class);
        when(group.getMembers()).thenReturn(members);
        return group;
    }

    /**
     * Returns a real User instance with the given ID set via reflection.
     * Avoids Mockito stubbing of getId() which is final in the User class.
     */
    private User userWithId(UUID id) {
        User user = new User();
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set User.id via reflection", e);
        }
        return user;
    }
}