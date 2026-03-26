package com.vibes.app.modules.messages.flyweight;

import java.util.UUID;

/**
 * Flyweight — holds the intrinsic (immutable, shareable) state of a user profile.
 *
 * In group chats the same sender can appear in hundreds of messages.
 * Rather than duplicating username + profilePictureUrl on every MessageView,
 * all MessageViews for the same sender share a single UserProfileFlyweight instance
 * held in the UserProfileFlyweightFactory cache.
 *
 * Intrinsic state (stored here, shared across messages):
 *   - senderId
 *   - senderUsername
 *   - senderProfilePictureUrl
 *
 * Extrinsic state (NOT stored here — passed in at render time):
 *   - senderStatus  (online / offline — changes frequently, must not be cached)
 */
public class UserProfileFlyweight {

    private final UUID senderId;
    private final String senderUsername;
    private final String senderProfilePictureUrl;

    public UserProfileFlyweight(UUID senderId, String senderUsername, String senderProfilePictureUrl) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderProfilePictureUrl = senderProfilePictureUrl;
    }

    public UUID getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public String getSenderProfilePictureUrl() { return senderProfilePictureUrl; }
}