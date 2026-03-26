package com.vibes.app.modules.auth.repositories;

import com.vibes.app.modules.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    boolean existsByUsername(String username);

    boolean existsByCredentialsEmail(String email);

    Optional<User> findByCredentialsEmail(String email);
}
