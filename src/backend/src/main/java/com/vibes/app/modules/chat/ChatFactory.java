package com.vibes.app.modules.chat;

import com.vibes.app.modules.auth.models.User;

import java.util.UUID;

public interface ChatFactory {
    Chat createChat(User user1, User user2);
    ChatSettings createSettings(UUID chatId);
}
