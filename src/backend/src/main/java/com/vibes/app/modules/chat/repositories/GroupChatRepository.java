package com.vibes.app.modules.chat.repositories;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.chat.group_chat.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, UUID> {

    @Query("SELECT g FROM GroupChat g JOIN g.members m WHERE m = :user")
    List<GroupChat> findAllByMember(@Param("user") User user);
}