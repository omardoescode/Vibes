package com.vibes.app.modules.notifications.presence;

import java.util.UUID;

public interface PresenceObserver {
    void onPresenceChanged(UUID userId, String username, boolean isOnline);
}
