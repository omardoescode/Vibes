package com.vibes.app.modules.chat;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Composite resolver that delegates to the appropriate resolver based on chat type.
 * Uses the Strategy pattern to resolve participants without knowing concrete chat types.
 */
@Component
@Primary
public class CompositeChatParticipantResolver implements ChatParticipantResolver {

    private final Set<ChatParticipantResolver> resolvers;

    public CompositeChatParticipantResolver(Set<ChatParticipantResolver> resolvers) {
        // Filter out self to avoid circular dependency
        this.resolvers = resolvers.stream()
                .filter(r -> !(r instanceof CompositeChatParticipantResolver))
                .collect(Collectors.toSet());
    }

    @Override
    public List<UUID> resolveRecipients(UUID chatId, UUID senderId) {
        return findResolver(chatId)
                .map(resolver -> resolver.resolveRecipients(chatId, senderId))
                .orElseThrow(() -> new IllegalArgumentException("No resolver found for chat: " + chatId));
    }

    @Override
    public boolean canResolve(UUID chatId) {
        return findResolver(chatId).isPresent();
    }

    private Optional<ChatParticipantResolver> findResolver(UUID chatId) {
        return resolvers.stream()
                .filter(resolver -> resolver.canResolve(chatId))
                .findFirst();
    }
}
