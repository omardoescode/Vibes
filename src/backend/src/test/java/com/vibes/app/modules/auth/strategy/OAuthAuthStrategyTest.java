package com.vibes.app.modules.auth.strategy;

import com.vibes.app.modules.auth.dto.OAuthLoginResponse;
import com.vibes.app.modules.auth.services.OAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthAuthStrategyTest {

    @Mock
    private OAuthService oAuthService;

    private OAuthAuthStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new OAuthAuthStrategy(oAuthService);
    }

    @Test
    void supports_oauthType_returnsTrue() {
        assertThat(strategy.supports(AuthRequestType.OAUTH)).isTrue();
    }

    @Test
    void supports_credentialType_returnsFalse() {
        assertThat(strategy.supports(AuthRequestType.CREDENTIAL)).isFalse();
    }

    @Test
    void authenticate_delegatesToOAuthService_returnsSuccess() {
        UUID userId = UUID.randomUUID();
        OAuthLoginResponse response = new OAuthLoginResponse(userId, "bob", "bob@github.com", null);

        when(oAuthService.handleOAuthLogin("github", "code123")).thenReturn(response);

        AuthResult result = strategy.authenticate(AuthRequest.oauth("github", "code123"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("bob");
        assertThat(result.getEmail()).isEqualTo("bob@github.com");
        verify(oAuthService).handleOAuthLogin("github", "code123");
    }

    @Test
    void authenticate_oAuthServiceThrows_returnsFailure() {
        when(oAuthService.handleOAuthLogin(any(), any())).thenThrow(new RuntimeException("provider error"));

        AuthResult result = strategy.authenticate(AuthRequest.oauth("github", "bad_code"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("provider error");
    }
}
