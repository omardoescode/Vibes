package com.vibes.app.modules.auth.dto;

import java.util.UUID;

public class UserResponse {
    private UUID id;
    private String username;
    private String profilePictureUrl;
    private String status;

    public UserResponse(UUID id, String username, String profilePictureUrl, String status) {
        this.id = id;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
        this.status = status;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public String getStatus() { return status; }
}
