package com.vibes.app.modules.messages.repositories;

import com.vibes.app.modules.messages.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByChatIdOrderByTimestampAsc(String chatId);
}