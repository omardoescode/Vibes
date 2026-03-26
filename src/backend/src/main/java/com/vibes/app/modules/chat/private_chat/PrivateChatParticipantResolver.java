package com.vibes.app.modules.chat.private_chat;

import com.vibes.app.modules.chat.ChatParticipantResolver;
import com.vibes.app.modules.chat.repositories.PrivateChatRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class PrivateChatParticipantResolver implements ChatParticipantResolver {

    private final PrivateChatRepository privateChatRepository;

    public PrivateChatParticipantResolver(PrivateChatRepository privateChatRepository) {
        this.privateChatRepository = privateChatRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> resolveRecipients(UUID chatId, UUID senderId) {
        return privateChatRepository.findById(chatId)
                .map(chat -> {
                    UUID recipientId = chat.getUser1().getId().equals(senderId)
                            ? chat.getUser2().getId()
                            : chat.getUser1().getId();
                    return Collections.singletonList(recipientId);
                })
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canResolve(UUID chatId) {
        return privateChatRepository.existsById(chatId);
    }
}
