package com.vibes.app.modules.chat.repositories;

import com.vibes.app.modules.chat.group_chat.GroupChatSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupChatSettingsRepository extends JpaRepository<GroupChatSettings, Long> {
}