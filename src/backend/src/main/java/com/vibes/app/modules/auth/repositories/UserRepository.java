package com.vibes.app.modules.auth.repositories;

import com.vibes.app.modules.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    /**
     * Fuzzy search for usernames using PostgreSQL pg_trgm extension.
     * Finds usernames similar to the query (typo-tolerant).
     * Requires: CREATE EXTENSION IF NOT EXISTS pg_trgm;
     */
    @Query(value = "SELECT * FROM users u WHERE u.username % :query ORDER BY similarity(u.username, :query) DESC", nativeQuery = true)
    List<User> searchUsersFuzzy(@Param("query") String query);

    boolean existsByUsername(String username);

    boolean existsByCredentialsEmail(String email);

    Optional<User> findByCredentialsEmail(String email);
}
