package com.vibes.app.modules.chat.private_chat;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.chat.Chat;
import com.vibes.app.modules.chat.ChatFactory;
import com.vibes.app.modules.chat.ChatSettings;

import java.util.UUID;

public class PrivateChatFactory implements ChatFactory {

    private static volatile PrivateChatFactory instance;

    private PrivateChatFactory() {}

    public static PrivateChatFactory getInstance() {
        if (instance == null) {
            synchronized (PrivateChatFactory.class) {
                if (instance == null) {
                    instance = new PrivateChatFactory();
                }
            }
        }
        return instance;
    }

    @Override
    public Chat createChat(User user1, User user2) {
        return new PrivateChat(user1, user2);
    }

    @Override
    public ChatSettings createSettings(UUID chatId) {
        PrivateChatSettings settings = new PrivateChatSettings();
        settings.setChatId(chatId);
        return settings;
    }
}
