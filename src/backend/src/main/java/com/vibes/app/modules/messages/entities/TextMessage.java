package com.vibes.app.modules.messages.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TEXT")
public class TextMessage extends Message {

    private String textContent;

    public TextMessage() {}

    public TextMessage(String textContent, String senderId, String chatId) {
        this.textContent = textContent;
        this.setSenderId(senderId);
        this.setChatId(chatId);
    }

    @Override public String getContent() { return this.textContent; }
    @Override public String getType() { return "TEXT"; }
}