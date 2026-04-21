package com.vibes.app.modules.auth.strategy;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.models.UserCredentials;
import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialAuthStrategyTest {

    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CredentialAuthStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CredentialAuthStrategy(userCredentialsRepository, passwordEncoder);
    }

    @Test
    void supports_credentialType_returnsTrue() {
        assertThat(strategy.supports(AuthRequestType.CREDENTIAL)).isTrue();
    }

    @Test
    void supports_oauthType_returnsFalse() {
        assertThat(strategy.supports(AuthRequestType.OAUTH)).isFalse();
    }

    @Test
    void authenticate_validCredentials_returnsSuccess() {
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("alice");

        UserCredentials creds = mock(UserCredentials.class);
        when(creds.getEmail()).thenReturn("alice@example.com");
        when(creds.getPasswordHash()).thenReturn("hashed");
        when(creds.getUser()).thenReturn(user);

        when(userCredentialsRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(creds));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        AuthResult result = strategy.authenticate(AuthRequest.credential("alice@example.com", "secret"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void authenticate_emailNotFound_returnsFailure() {
        when(userCredentialsRepository.findByEmail(any())).thenReturn(Optional.empty());

        AuthResult result = strategy.authenticate(AuthRequest.credential("nobody@example.com", "pass"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();
    }

    @Test
    void authenticate_wrongPassword_returnsFailure() {
        UserCredentials creds = mock(UserCredentials.class);
        when(creds.getPasswordHash()).thenReturn("hashed");

        when(userCredentialsRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(creds));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        AuthResult result = strategy.authenticate(AuthRequest.credential("alice@example.com", "wrong"));

        assertThat(result.isSuccess()).isFalse();
    }
}
