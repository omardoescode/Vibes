package com.vibes.app.modules.chat.services;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import com.vibes.app.modules.chat.group_chat.GroupChat;
import com.vibes.app.modules.chat.mediator.GroupChatMediator;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.chat.repositories.GroupChatSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifies that GroupChatService calls the mediator:
 * - after a successful save() — not before
 * - never when a validation guard throws before reaching persistence
 */
@ExtendWith(MockitoExtension.class)
class GroupChatServiceMediatorTest {

    @Mock private GroupChatRepository groupChatRepository;
    @Mock private GroupChatSettingsRepository settingsRepository;
    @Mock private UserRepository userRepository;
    @Mock private GroupChatMediator mediator;

    private GroupChatService service;

    private final UUID groupId     = UUID.randomUUID();
    private final UUID creatorId   = UUID.randomUUID();
    private final UUID memberId    = UUID.randomUUID();
    private final UUID newMemberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GroupChatService(
                groupChatRepository, settingsRepository, userRepository, mediator);
    }

    // -------------------------------------------------------------------------
    // addMember
    // -------------------------------------------------------------------------

    @Test
    void addMember_mediatorCalledAfterSave() {
        User creator   = userWithId(creatorId);
        User member    = userWithId(memberId);
        User newMember = userWithId(newMemberId);

        GroupChat group = groupWithCreatorAndMember(creator, member);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(newMemberId)).thenReturn(Optional.of(newMember));
        when(groupChatRepository.save(any(GroupChat.class))).thenReturn(group);

        service.addMember(groupId, creatorId, newMemberId);

        InOrder order = inOrder(groupChatRepository, mediator);
        order.verify(groupChatRepository).save(any(GroupChat.class));
        order.verify(mediator).onMemberAdded(groupId, newMemberId);
    }

    @Test
    void addMember_nonAdminRequester_mediatorNeverCalled() {
        UUID nonAdminId = UUID.randomUUID();
        User creator = userWithId(creatorId);
        User member  = userWithId(memberId);

        GroupChat group = groupWithCreatorAndMember(creator, member);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> service.addMember(groupId, nonAdminId, newMemberId))
                .isInstanceOf(SecurityException.class);

        verifyNoInteractions(mediator);
    }

    @Test
    void addMember_groupNotFound_mediatorNeverCalled() {
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMember(groupId, creatorId, newMemberId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(mediator);
    }

    // -------------------------------------------------------------------------
    // removeMember
    // -------------------------------------------------------------------------

    @Test
    void removeMember_mediatorCalledAfterSave() {
        User creator = userWithId(creatorId);
        User member  = userWithId(memberId);

        GroupChat group = groupWithCreatorAndMember(creator, member);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(groupChatRepository.save(any(GroupChat.class))).thenReturn(group);

        service.removeMember(groupId, creatorId, memberId);

        InOrder order = inOrder(groupChatRepository, mediator);
        order.verify(groupChatRepository).save(any(GroupChat.class));
        order.verify(mediator).onMemberRemoved(groupId, memberId);
    }

    @Test
    void removeMember_nonAdminRequester_mediatorNeverCalled() {
        UUID nonAdminId = UUID.randomUUID();
        User creator = userWithId(creatorId);
        User member  = userWithId(memberId);

        GroupChat group = groupWithCreatorAndMember(creator, member);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> service.removeMember(groupId, nonAdminId, memberId))
                .isInstanceOf(SecurityException.class);

        verifyNoInteractions(mediator);
    }

    @Test
    void removeMember_creatorRemovesThemselves_mediatorNeverCalled() {
        User creator = userWithId(creatorId);
        User member  = userWithId(memberId);

        GroupChat group = groupWithCreatorAndMember(creator, member);
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> service.removeMember(groupId, creatorId, creatorId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(mediator);
    }

    @Test
    void removeMember_groupNotFound_mediatorNeverCalled() {
        when(groupChatRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeMember(groupId, creatorId, memberId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(mediator);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a mocked GroupChat with the given real User objects.
     * Uses a mocked GroupChat so we fully control getCreator() and getMembers()
     * without the real constructor's side effects.
     */
    private GroupChat groupWithCreatorAndMember(User creator, User member) {
        GroupChat group = mock(GroupChat.class);
        lenient().when(group.getCreator()).thenReturn(creator);
        lenient().when(group.getMembers()).thenReturn(new ArrayList<>(List.of(creator, member)));
        return group;
    }

    /**
     * Constructs a real User and sets its id field via reflection.
     * Required because User.getId() is final and cannot be stubbed by Mockito.
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