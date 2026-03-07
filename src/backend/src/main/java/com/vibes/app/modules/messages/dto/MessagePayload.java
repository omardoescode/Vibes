package com.vibes.app.modules.messages.dto;

import org.springframework.web.multipart.MultipartFile;

public class MessagePayload {
    private String chatId;
    private String senderId;
    private String textContent;
    private MultipartFile fileContent;

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public MultipartFile getFileContent() { return fileContent; }
    public void setFileContent(MultipartFile fileContent) { this.fileContent = fileContent; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
}