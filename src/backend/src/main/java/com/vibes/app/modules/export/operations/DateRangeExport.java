package com.vibes.app.modules.export.operations;

import com.vibes.app.modules.export.bridge.ExportFormatter;
import com.vibes.app.modules.export.bridge.ExportOperation;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.repositories.MessageRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Refined Abstraction: Exports messages within a specific date range.
 */
public class DateRangeExport extends ExportOperation {
    
    private final String chatId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final MessageRepository messageRepository;
    
    public DateRangeExport(ExportFormatter formatter, String chatId, 
                          LocalDate startDate, LocalDate endDate,
                          MessageRepository messageRepository) {
        super(formatter);
        this.chatId = chatId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.messageRepository = messageRepository;
    }
    
    @Override
    public String execute() {
        List<Message> messages = getMessages();
        return formatter.format(messages);
    }
    
    @Override
    protected List<Message> getMessages() {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId).stream()
            .filter(msg -> !msg.getTimestamp().isBefore(startDateTime))
            .filter(msg -> !msg.getTimestamp().isAfter(endDateTime))
            .collect(Collectors.toList());
    }
    
    public String getChatId() {
        return chatId;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
}