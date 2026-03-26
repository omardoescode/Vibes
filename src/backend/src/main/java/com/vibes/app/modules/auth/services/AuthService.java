package com.vibes.app.modules.auth.services;

import com.vibes.app.modules.auth.dto.RegisterRequest;
import com.vibes.app.modules.auth.dto.UserResponse;
import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.models.UserCredentials;
import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import com.vibes.app.modules.auth.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       UserCredentialsRepository userCredentialsRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userCredentialsRepository = userCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userCredentialsRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setStatus("online");
        User savedUser = userRepository.save(user);

        UserCredentials credentials = new UserCredentials();
        credentials.setEmail(request.getEmail());
        credentials.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credentials.setUser(savedUser);
        userCredentialsRepository.save(credentials);

        return toResponse(savedUser);
    }

    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(user);
    }

    public UserResponse getProfileByEmail(String email) {
        UserCredentials credentials = userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(credentials.getUser());
    }

    public UserResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(user);
    }

    @Transactional
    public void updateStatus(UUID userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateProfilePicture(String email, String profilePictureUrl) {
        UserCredentials credentials = userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User user = credentials.getUser();
        user.setProfilePictureUrl(profilePictureUrl);
        userRepository.save(user);
        return toResponse(user);
    }

    public List<UserResponse> searchUsers(String query) {
        return userRepository.searchUsersFuzzy(query)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(),
                user.getProfilePictureUrl(), user.getStatus());
    }
}
