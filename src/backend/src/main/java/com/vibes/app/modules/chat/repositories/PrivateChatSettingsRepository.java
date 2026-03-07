package com.vibes.app.modules.chat.repositories;

import com.vibes.app.modules.chat.private_chat.PrivateChatSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivateChatSettingsRepository extends JpaRepository<PrivateChatSettings, Long> {
}
