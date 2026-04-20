package com.vibes.app.modules.auth.strategy;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthenticationContextTest {

    @Test
    void authenticate_routesToMatchingStrategy() {
        AuthenticationStrategy credStrategy = mock(AuthenticationStrategy.class);
        AuthenticationStrategy oauthStrategy = mock(AuthenticationStrategy.class);

        when(credStrategy.supports(AuthRequestType.CREDENTIAL)).thenReturn(true);
        when(credStrategy.supports(AuthRequestType.OAUTH)).thenReturn(false);
        when(oauthStrategy.supports(AuthRequestType.CREDENTIAL)).thenReturn(false);
        when(oauthStrategy.supports(AuthRequestType.OAUTH)).thenReturn(true);

        AuthResult expected = AuthResult.success(UUID.randomUUID(), "alice", "alice@example.com");
        when(credStrategy.authenticate(any())).thenReturn(expected);

        AuthenticationContext context = new AuthenticationContext(List.of(credStrategy, oauthStrategy));
        AuthRequest request = AuthRequest.credential("alice@example.com", "pass");

        AuthResult result = context.authenticate(request);

        assertThat(result).isSameAs(expected);
        verify(credStrategy).authenticate(request);
        verifyNoInteractions(oauthStrategy);
    }

    @Test
    void authenticate_noMatchingStrategy_returnsFailure() {
        AuthenticationStrategy credStrategy = mock(AuthenticationStrategy.class);
        when(credStrategy.supports(any())).thenReturn(false);

        AuthenticationContext context = new AuthenticationContext(List.of(credStrategy));
        AuthResult result = context.authenticate(AuthRequest.oauth("github", "code"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("No authentication strategy");
    }

    @Test
    void authenticate_oauthType_routesToOAuthStrategy() {
        AuthenticationStrategy oauthStrategy = mock(AuthenticationStrategy.class);
        when(oauthStrategy.supports(AuthRequestType.OAUTH)).thenReturn(true);

        UUID userId = UUID.randomUUID();
        AuthResult expected = AuthResult.success(userId, "bob", "bob@gh.com");
        when(oauthStrategy.authenticate(any())).thenReturn(expected);

        AuthenticationContext context = new AuthenticationContext(List.of(oauthStrategy));
        AuthResult result = context.authenticate(AuthRequest.oauth("github", "code123"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getUserId()).isEqualTo(userId);
    }
}
