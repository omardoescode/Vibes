package com.vibes.app.modules.notifications.presence;

import com.vibes.app.modules.websocket.ChatSessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionRegistryPresenceObserverTest {

    @Mock
    private ChatSessionRegistry chatSessionRegistry;

    private SessionRegistryPresenceObserver observer;

    @BeforeEach
    void setUp() {
        observer = new SessionRegistryPresenceObserver(chatSessionRegistry);
    }

    @Test
    void onPresenceChanged_offline_closesChat() {
        UUID userId = UUID.randomUUID();

        observer.onPresenceChanged(userId, "testuser", false);

        verify(chatSessionRegistry).closeChat(userId.toString());
    }

    @Test
    void onPresenceChanged_online_doesNotCloseChat() {
        UUID userId = UUID.randomUUID();

        observer.onPresenceChanged(userId, "testuser", true);

        verifyNoInteractions(chatSessionRegistry);
    }
}
