package com.vibes.app.modules.messages.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ATTACHMENT")
public class AttachmentMessage extends Message {

    private String viewLink;

    public AttachmentMessage() {}

    public AttachmentMessage(String viewLink, String senderId, String chatId) {
        this.viewLink = viewLink;
        this.setSenderId(senderId);
        this.setChatId(chatId);
    }

    @Override public String getContent() { return this.viewLink; }
    @Override public String getType() { return "ATTACHMENT"; }
}