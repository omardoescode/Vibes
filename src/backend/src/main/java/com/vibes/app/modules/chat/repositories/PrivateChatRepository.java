package com.vibes.app.modules.chat.repositories;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.chat.private_chat.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivateChatRepository extends JpaRepository<PrivateChat, UUID> {

    @Query("SELECT c FROM PrivateChat c WHERE (c.user1 = :user OR c.user2 = :user)")
    List<PrivateChat> findAllByUser(@Param("user") User user);

    @Query("SELECT c FROM PrivateChat c WHERE (c.user1 = :u1 AND c.user2 = :u2) OR (c.user1 = :u2 AND c.user2 = :u1)")
    Optional<PrivateChat> findByUsers(@Param("u1") User u1, @Param("u2") User u2);
}
