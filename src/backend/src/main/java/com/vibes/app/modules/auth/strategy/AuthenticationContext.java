package com.vibes.app.modules.auth.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthenticationContext {

    private final List<AuthenticationStrategy> strategies;

    public AuthenticationContext(List<AuthenticationStrategy> strategies) {
        this.strategies = strategies;
    }

    public AuthResult authenticate(AuthRequest request) {
        return strategies.stream()
                .filter(s -> s.supports(request.getType()))
                .findFirst()
                .map(s -> s.authenticate(request))
                .orElse(AuthResult.failure("No authentication strategy found for type: " + request.getType()));
    }
}
