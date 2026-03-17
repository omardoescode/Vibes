package com.vibes.app.modules.chat.group_chat;

import com.vibes.app.modules.auth.models.User;

import java.util.List;
import java.util.UUID;

/**
 * Singleton factory for creating GroupChat instances and their per-member settings.
 * Uses a separate interface from ChatFactory since group creation requires
 * a name and a list of members rather than exactly two users.
 */
public class GroupChatFactory {

    private static volatile GroupChatFactory instance;

    private GroupChatFactory() {}

    public static GroupChatFactory getInstance() {
        if (instance == null) {
            synchronized (GroupChatFactory.class) {
                if (instance == null) {
                    instance = new GroupChatFactory();
                }
            }
        }
        return instance;
    }

    public GroupChat createChat(String name, User creator, List<User> members) {
        return new GroupChat(name, creator, members);
    }

    /**
     * Creates a settings row for one member of the group.
     * Call once per member when the group is first created.
     */
    public GroupChatSettings createSettings(UUID chatId, UUID userId) {
        GroupChatSettings settings = new GroupChatSettings();
        settings.setChatId(chatId);
        settings.setUserId(userId);
        return settings;
    }
}