package com.vibes.app.modules.chat.services;

import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.repositories.UserRepository;
import com.vibes.app.modules.chat.dto.ChatResponse;
import com.vibes.app.modules.chat.private_chat.PrivateChat;
import com.vibes.app.modules.chat.private_chat.PrivateChatFactory;
import com.vibes.app.modules.chat.private_chat.PrivateChatSettings;
import com.vibes.app.modules.chat.repositories.PrivateChatRepository;
import com.vibes.app.modules.chat.repositories.PrivateChatSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final PrivateChatRepository chatRepository;
    private final PrivateChatSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    public ChatService(PrivateChatRepository chatRepository,
                       PrivateChatSettingsRepository settingsRepository,
                       UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get or create a private chat between two users.
     * Called when a user wants to send the first message or open an existing conversation.
     */
    @Transactional
    public ChatResponse getOrCreateChat(UUID currentUserId, UUID targetUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        PrivateChat chat = chatRepository.findByUsers(currentUser, targetUser)
                .orElseGet(() -> {
                    PrivateChat newChat = (PrivateChat) PrivateChatFactory.getInstance().createChat(currentUser, targetUser);
                    PrivateChat saved = chatRepository.save(newChat);

                    PrivateChatSettings settings1 = (PrivateChatSettings) PrivateChatFactory.getInstance().createSettings(saved.getId());
                    PrivateChatSettings settings2 = (PrivateChatSettings) PrivateChatFactory.getInstance().createSettings(saved.getId());
                    settingsRepository.save(settings1);
                    settingsRepository.save(settings2);

                    return saved;
                });

        return toResponse(chat, currentUser);
    }

    /**
     * List all chats for the given user.
     */
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return chatRepository.findAllByUser(user)
                .stream()
                .map(chat -> toResponse(chat, user))
                .collect(Collectors.toList());
    }

    private ChatResponse toResponse(PrivateChat chat, User currentUser) {
        User other = chat.getUser1().getId().equals(currentUser.getId())
                ? chat.getUser2()
                : chat.getUser1();
        return new ChatResponse(
                chat.getId(),
                other.getId(),
                other.getUsername(),
                other.getProfilePictureUrl(),
                chat.getCreatedAt()
        );
    }
}
