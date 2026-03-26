package com.vibes.app.modules.chat.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ChatResponse {
    private UUID chatId;
    private String type; // "PRIVATE" or "GROUP"
    
    // Private chat fields
    private UUID otherUserId;
    private String otherUsername;
    private String otherUserProfilePicture;
    
    // Group chat fields
    private String name;
    private String groupPictureUrl;
    private UUID creatorId;
    private List<MemberInfo> members;
    private int memberCount;
    
    private LocalDateTime createdAt;

    // Private chat constructor
    public ChatResponse(UUID chatId, UUID otherUserId, String otherUsername,
                        String otherUserProfilePicture, LocalDateTime createdAt) {
        this.chatId = chatId;
        this.type = "PRIVATE";
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.otherUserProfilePicture = otherUserProfilePicture;
        this.createdAt = createdAt;
    }

    // Group chat constructor
    public ChatResponse(UUID chatId, String name, String groupPictureUrl,
                        UUID creatorId, List<MemberInfo> members, int memberCount, LocalDateTime createdAt) {
        this.chatId = chatId;
        this.type = "GROUP";
        this.name = name;
        this.groupPictureUrl = groupPictureUrl;
        this.creatorId = creatorId;
        this.members = members;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }

    public UUID getChatId() { return chatId; }
    public String getType() { return type; }
    public UUID getOtherUserId() { return otherUserId; }
    public String getOtherUsername() { return otherUsername; }
    public String getOtherUserProfilePicture() { return otherUserProfilePicture; }
    public String getName() { return name; }
    public String getGroupPictureUrl() { return groupPictureUrl; }
    public UUID getCreatorId() { return creatorId; }
    public List<MemberInfo> getMembers() { return members; }
    public int getMemberCount() { return memberCount; }
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
