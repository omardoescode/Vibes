package com.vibes.app.modules.auth.repositories;

import com.vibes.app.modules.auth.models.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByEmail(String email);

    Optional<UserCredentials> findByUserId(UUID userId);

    boolean existsByEmail(String email);
}
