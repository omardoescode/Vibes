package com.vibes.app.modules.auth.services;

import com.vibes.app.modules.auth.models.UserCredentials;
import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserCredentialsRepository userCredentialsRepository;

    public UserDetailsServiceImpl(UserCredentialsRepository userCredentialsRepository) {
        this.userCredentialsRepository = userCredentialsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentials credentials = userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new User(credentials.getEmail(), credentials.getPasswordHash(), List.of());
    }
}
