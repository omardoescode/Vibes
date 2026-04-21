package com.vibes.app.modules.auth.strategy;

import java.util.UUID;

public class AuthResult {
    private final UUID userId;
    private final String username;
    private final String email;
    private final boolean success;
    private final String errorMessage;

    private AuthResult(UUID userId, String username, String email, boolean success, String errorMessage) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static AuthResult success(UUID userId, String username, String email) {
        return new AuthResult(userId, username, email, true, null);
    }

    public static AuthResult failure(String errorMessage) {
        return new AuthResult(null, null, null, false, errorMessage);
    }

    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
