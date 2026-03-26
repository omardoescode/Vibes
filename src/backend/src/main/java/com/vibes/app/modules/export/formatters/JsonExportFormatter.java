package com.vibes.app.modules.export.formatters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vibes.app.modules.export.bridge.ExportFormatter;
import com.vibes.app.modules.messages.entities.Message;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Concrete Implementor: Exports messages as structured JSON.
 */
@Component
public class JsonExportFormatter implements ExportFormatter {
    
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter;
    
    public JsonExportFormatter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }
    
    @Override
    public String format(List<Message> messages) {
        List<Map<String, Object>> messageMaps = messages.stream()
            .map(this::messageToMap)
            .collect(Collectors.toList());
        
        try {
            return objectMapper.writeValueAsString(messageMaps);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to format messages as JSON", e);
        }
    }
    
    private Map<String, Object> messageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("chatId", message.getChatId());
        map.put("senderId", message.getSenderId());
        map.put("timestamp", message.getTimestamp().format(dateFormatter));
        map.put("type", message.getType());
        map.put("content", message.getContent());
        return map;
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
    
    @Override
    public String getFileExtension() {
        return "json";
    }
}