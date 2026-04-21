package com.vibes.app.modules.auth.strategy;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.models.UserCredentials;
import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CredentialAuthStrategy implements AuthenticationStrategy {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;

    public CredentialAuthStrategy(UserCredentialsRepository userCredentialsRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userCredentialsRepository = userCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean supports(AuthRequestType type) {
        return type == AuthRequestType.CREDENTIAL;
    }

    @Override
    public AuthResult authenticate(AuthRequest request) {
        Optional<UserCredentials> credentials = userCredentialsRepository.findByEmail(request.getEmail());
        if (credentials.isEmpty()) {
            return AuthResult.failure("Invalid credentials");
        }

        UserCredentials creds = credentials.get();
        if (!passwordEncoder.matches(request.getPassword(), creds.getPasswordHash())) {
            return AuthResult.failure("Invalid credentials");
        }

        User user = creds.getUser();
        return AuthResult.success(user.getId(), user.getUsername(), creds.getEmail());
    }
}
