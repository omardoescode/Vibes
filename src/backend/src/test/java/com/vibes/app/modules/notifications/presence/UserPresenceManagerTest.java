package com.vibes.app.modules.notifications.presence;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPresenceManagerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PresenceObserver observer1;

    @Mock
    private PresenceObserver observer2;

    private UserPresenceManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserPresenceManager(List.of(observer1, observer2), userRepository);
    }

    @Test
    void setOffline_updatesDbStatusAndNotifiesAllObservers() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        manager.setOffline(userId);

        verify(userRepository).save(user);
        verify(observer1).onPresenceChanged(userId, "testuser", false);
        verify(observer2).onPresenceChanged(userId, "testuser", false);
    }

    @Test
    void setOnline_updatesDbStatusAndNotifiesAllObservers() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        manager.setOnline(userId);

        verify(userRepository).save(user);
        verify(observer1).onPresenceChanged(userId, "testuser", true);
        verify(observer2).onPresenceChanged(userId, "testuser", true);
    }

    @Test
    void setOffline_continuesNotifyingRemainingObserversWhenOneThrows() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("observer error")).when(observer1).onPresenceChanged(any(), any(), anyBoolean());

        manager.setOffline(userId);

        verify(observer2).onPresenceChanged(userId, "testuser", false);
    }
}
