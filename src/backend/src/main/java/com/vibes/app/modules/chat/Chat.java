package com.vibes.app.modules.chat;

import com.vibes.app.modules.messages.Message;

public interface Chat {

    void sendMessage(Message message);

    Message receiveMessage();

    Message editMessage(Message message);

}