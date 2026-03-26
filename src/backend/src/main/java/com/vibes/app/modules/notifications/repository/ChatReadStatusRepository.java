package com.vibes.app.modules.notifications.repository;

import com.vibes.app.modules.notifications.model.ChatReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

  Optional<ChatReadStatus> findByUserIdAndChatId(UUID userId, UUID chatId);

  List<ChatReadStatus> findByUserId(UUID userId);

  boolean existsByUserIdAndChatId(UUID userId, UUID chatId);
}
