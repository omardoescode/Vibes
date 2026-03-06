package com.vibes.app.modules.chat.private_chat;

import jakarta.persistence.*;
import com.vibes.app.modules.chat.Chat;
import com.vibes.app.modules.messages.Message;

@Entity
@Table(name = "private_chats")
public class PrivateChat implements Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long user1Id;
    private Long user2Id;

    @Override
    public void sendMessage(Message message) {
        System.out.println("Private message from " + message.getSenderId() + ": " + message.getContent());
    }

    @Override
    public Message receiveMessage() {
        return new Message("user2", "Greetings!");
    }

    @Override
    public Message editMessage(Message message) {
        return message;
    }

    public Long getId() { return id; }
    public Long getUser1Id() { return user1Id; }
    public void setUser1Id(Long user1Id) { this.user1Id = user1Id; }
    public Long getUser2Id() { return user2Id; }
    public void setUser2Id(Long user2Id) { this.user2Id = user2Id; }
}