package com.vibes.app.modules.messages.flyweight;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory + cache for UserProfileFlyweight instances.
 *
 * Keyed by user UUID. On first access the flyweight is built from the database
 * and stored in the map. Subsequent calls for the same userId return the cached
 * instance — no DB round-trip, no object duplication.
 *
 * Cache invalidation: call {@link #evict(UUID)} whenever a user updates their
 * username or profile picture so the next message fetch reflects the new values.
 */
@Component
public class UserProfileFlyweightFactory {

    private final ConcurrentHashMap<UUID, UserProfileFlyweight> cache = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public UserProfileFlyweightFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a cached flyweight for the given userId, loading from DB if needed.
     */
    public UserProfileFlyweight get(UUID userId) {
        return cache.computeIfAbsent(userId, id -> {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
            return new UserProfileFlyweight(user.getId(), user.getUsername(), user.getProfilePictureUrl());
        });
    }

    /**
     * Removes the cached flyweight for a user (call after profile updates).
     */
    public void evict(UUID userId) {
        cache.remove(userId);
    }

    public int cacheSize() {
        return cache.size();
    }
}