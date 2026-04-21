package com.vibes.app.modules.notifications.presence;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class UserPresenceManager {

    private static final Logger log = LoggerFactory.getLogger(UserPresenceManager.class);

    private final List<PresenceObserver> observers;
    private final UserRepository userRepository;

    public UserPresenceManager(List<PresenceObserver> observers, UserRepository userRepository) {
        this.observers = observers;
        this.userRepository = userRepository;
    }

    @Transactional
    public void setOnline(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setStatus("online");
        userRepository.save(user);
        log.debug("[presence] user={} is now online", userId);
        notifyObservers(userId, user.getUsername(), true);
    }

    @Transactional
    public void setOffline(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setStatus("offline");
        userRepository.save(user);
        log.debug("[presence] user={} is now offline", userId);
        notifyObservers(userId, user.getUsername(), false);
    }

    private void notifyObservers(UUID userId, String username, boolean isOnline) {
        for (PresenceObserver observer : observers) {
            try {
                observer.onPresenceChanged(userId, username, isOnline);
            } catch (Exception e) {
                log.warn("[presence] observer {} failed for user={}: {}", observer.getClass().getSimpleName(), userId, e.getMessage());
            }
        }
    }
}
