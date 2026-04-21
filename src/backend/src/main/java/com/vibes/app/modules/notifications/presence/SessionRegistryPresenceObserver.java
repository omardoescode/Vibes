package com.vibes.app.modules.notifications.presence;

import com.vibes.app.modules.websocket.ChatSessionRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SessionRegistryPresenceObserver implements PresenceObserver {

    private final ChatSessionRegistry chatSessionRegistry;

    public SessionRegistryPresenceObserver(ChatSessionRegistry chatSessionRegistry) {
        this.chatSessionRegistry = chatSessionRegistry;
    }

    @Override
    public void onPresenceChanged(UUID userId, String username, boolean isOnline) {
        if (!isOnline) {
            chatSessionRegistry.closeChat(userId.toString());
        }
    }
}
