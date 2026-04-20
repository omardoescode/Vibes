package com.vibes.app.modules.auth.strategy;

import com.vibes.app.modules.auth.dto.OAuthLoginResponse;
import com.vibes.app.modules.auth.services.OAuthService;
import org.springframework.stereotype.Component;

@Component
public class OAuthAuthStrategy implements AuthenticationStrategy {

    private final OAuthService oAuthService;

    public OAuthAuthStrategy(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @Override
    public boolean supports(AuthRequestType type) {
        return type == AuthRequestType.OAUTH;
    }

    @Override
    public AuthResult authenticate(AuthRequest request) {
        try {
            OAuthLoginResponse response = oAuthService.handleOAuthLogin(request.getProvider(), request.getCode());
            return AuthResult.success(response.getId(), response.getUsername(), response.getEmail());
        } catch (Exception e) {
            return AuthResult.failure(e.getMessage());
        }
    }
}
