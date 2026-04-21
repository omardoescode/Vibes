package com.vibes.app.modules.chat.mediator;

import java.util.UUID;

public interface GroupChatMediator {
    void onMemberAdded(UUID groupID, UUID addedUserId);

    void onMemberRemoved(UUID groupID, UUID removedUserId);
}