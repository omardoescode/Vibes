package com.vibes.app.modules.auth.strategy;

public interface AuthenticationStrategy {
    boolean supports(AuthRequestType type);
    AuthResult authenticate(AuthRequest request);
}
