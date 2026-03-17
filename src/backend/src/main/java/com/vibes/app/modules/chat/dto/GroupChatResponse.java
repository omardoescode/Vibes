package com.vibes.app.modules.chat.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class GroupChatResponse {
    private UUID chatId;
    private String name;
    private String groupPictureUrl;
    private UUID creatorId;
    private List<MemberInfo> members;
    private LocalDateTime createdAt;

    public GroupChatResponse(UUID chatId, String name, String groupPictureUrl,
                              UUID creatorId, List<MemberInfo> members, LocalDateTime createdAt) {
        this.chatId = chatId;
        this.name = name;
        this.groupPictureUrl = groupPictureUrl;
        this.creatorId = creatorId;
        this.members = members;
        this.createdAt = createdAt;
    }

    public UUID getChatId() { return chatId; }
    public String getName() { return name; }
    public String getGroupPictureUrl() { return groupPictureUrl; }
    public UUID getCreatorId() { return creatorId; }
    public List<MemberInfo> getMembers() { return members; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Lightweight member summary embedded in the group response.
     */
    public static class MemberInfo {
        private UUID userId;
        private String username;
        private String profilePictureUrl;

        public MemberInfo(UUID userId, String username, String profilePictureUrl) {
            this.userId = userId;
            this.username = username;
            this.profilePictureUrl = profilePictureUrl;
        }

        public UUID getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getProfilePictureUrl() { return profilePictureUrl; }
    }
}