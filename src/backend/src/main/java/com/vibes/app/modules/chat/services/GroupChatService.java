package com.vibes.app.modules.chat.services;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import com.vibes.app.modules.chat.dto.CreateGroupRequest;
import com.vibes.app.modules.chat.dto.GroupChatResponse;
import com.vibes.app.modules.chat.group_chat.GroupChat;
import com.vibes.app.modules.chat.group_chat.GroupChatFactory;
import com.vibes.app.modules.chat.group_chat.GroupChatSettings;
import com.vibes.app.modules.chat.mediator.GroupChatMediator;
import com.vibes.app.modules.chat.repositories.GroupChatRepository;
import com.vibes.app.modules.chat.repositories.GroupChatSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final GroupChatSettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final GroupChatMediator groupChatMediator;

    public GroupChatService(GroupChatRepository groupChatRepository,
                            GroupChatSettingsRepository settingsRepository,
                            UserRepository userRepository,
                            GroupChatMediator groupChatMediator) {
        this.groupChatRepository = groupChatRepository;
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
        this.groupChatMediator = groupChatMediator;
    }

    /**
     * Creates a new group chat. The authenticated user becomes the creator/admin.
     * Creator is automatically included in the members list.
     */
    @Transactional
    public GroupChatResponse createGroup(UUID creatorId, CreateGroupRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        List<User> members = request.getMemberIds().stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id)))
                .collect(Collectors.toList());

        GroupChat group = GroupChatFactory.getInstance().createChat(request.getName(), creator, members);
        GroupChat saved = groupChatRepository.save(group);

        // Create per-member settings for every participant (including creator)
        saved.getMembers().forEach(member -> {
            GroupChatSettings settings = GroupChatFactory.getInstance()
                    .createSettings(saved.getId(), member.getId());
            settingsRepository.save(settings);
        });

        return toResponse(saved);
    }

    /**
     * Returns all group chats the given user is a member of.
     */
    public List<GroupChatResponse> getGroupsForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return groupChatRepository.findAllByMember(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new member to an existing group. Only the creator (admin) can do this.
     * Notifies existing members via the mediator after persistence succeeds.
     */
    @Transactional
    public GroupChatResponse addMember(UUID groupId, UUID requesterId, UUID newMemberId) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (!group.getCreator().getId().equals(requesterId)) {
            throw new SecurityException("Only the group admin can add members");
        }

        User newMember = userRepository.findById(newMemberId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + newMemberId));

        group.addMember(newMember);
        GroupChatSettings settings = GroupChatFactory.getInstance()
                .createSettings(groupId, newMemberId);
        settingsRepository.save(settings);

        GroupChatResponse response = toResponse(groupChatRepository.save(group));

        // Notify existing members only after persistence has succeeded
        groupChatMediator.onMemberAdded(groupId, newMemberId);

        return response;
    }

    /**
     * Removes a member from the group. Only the creator (admin) can do this.
     * The creator cannot remove themselves.
     * Notifies remaining members via the mediator after persistence succeeds.
     */
    @Transactional
    public GroupChatResponse removeMember(UUID groupId, UUID requesterId, UUID targetMemberId) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (!group.getCreator().getId().equals(requesterId)) {
            throw new SecurityException("Only the group admin can remove members");
        }

        if (group.getCreator().getId().equals(targetMemberId)) {
            throw new IllegalArgumentException("Admin cannot remove themselves from the group");
        }

        User target = userRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetMemberId));

        group.removeMember(target);
        GroupChatResponse response = toResponse(groupChatRepository.save(group));

        // Notify remaining members only after persistence has succeeded
        groupChatMediator.onMemberRemoved(groupId, targetMemberId);

        return response;
    }

    private GroupChatResponse toResponse(GroupChat group) {
        List<GroupChatResponse.MemberInfo> memberInfos = group.getMembers().stream()
                .map(m -> new GroupChatResponse.MemberInfo(
                        m.getId(), m.getUsername(), m.getProfilePictureUrl()))
                .collect(Collectors.toList());

        return new GroupChatResponse(
                group.getId(),
                group.getName(),
                group.getGroupPictureUrl(),
                group.getCreator().getId(),
                memberInfos,
                group.getCreatedAt()
        );
    }
}