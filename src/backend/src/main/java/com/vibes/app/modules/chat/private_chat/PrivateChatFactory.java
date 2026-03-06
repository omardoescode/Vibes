package com.vibes.app.modules.chat.private_chat;

import com.vibes.app.modules.chat.Chat;
import com.vibes.app.modules.chat.ChatFactory;
import com.vibes.app.modules.chat.ChatSettings;

public class PrivateChatFactory implements ChatFactory {

    @Override
    public Chat createChat() {
        return new PrivateChat();
    }

    @Override
    public ChatSettings createSettings() {
        return new PrivateChatSettings();
    }
}