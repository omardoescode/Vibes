package com.vibes.app.modules.export.formatters;

import com.vibes.app.modules.export.bridge.ExportFormatter;
import com.vibes.app.modules.messages.entities.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Implementor: Exports messages as CSV.
 */
@Component
public class CsvExportFormatter implements ExportFormatter {
    
    private static final String HEADER = "id,chatId,senderId,timestamp,type,content\n";
    
    @Override
    public String format(List<Message> messages) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER);
        
        for (Message message : messages) {
            csv.append(escapeField(message.getId())).append(",")
               .append(escapeField(message.getChatId())).append(",")
               .append(escapeField(message.getSenderId())).append(",")
               .append(escapeField(message.getTimestamp().toString())).append(",")
               .append(escapeField(message.getType())).append(",")
               .append(escapeField(message.getContent())).append("\n");
        }
        
        return csv.toString();
    }
    
    private String escapeField(String field) {
        if (field == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma or newline
        String escaped = field.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
    
    @Override
    public String getMimeType() {
        return "text/csv";
    }
    
    @Override
    public String getFileExtension() {
        return "csv";
    }
}