package com.vibes.app.modules.messages.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FILE")
public class FileMessage extends Message {

    private String downloadLink;

    public FileMessage() {}

    public FileMessage(String downloadLink, String senderId, String chatId) {
        this.downloadLink = downloadLink;
        this.setSenderId(senderId);
        this.setChatId(chatId);
    }

    @Override public String getContent() { return this.downloadLink; }
    @Override public String getType() { return "FILE"; }
}