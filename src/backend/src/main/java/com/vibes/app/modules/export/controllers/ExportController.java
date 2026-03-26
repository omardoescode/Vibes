package com.vibes.app.modules.export.controllers;

import com.vibes.app.modules.export.bridge.ExportFormatter;
import com.vibes.app.modules.export.formatters.CsvExportFormatter;
import com.vibes.app.modules.export.formatters.JsonExportFormatter;
import com.vibes.app.modules.export.operations.DateRangeExport;
import com.vibes.app.modules.export.operations.FullChatExport;
import com.vibes.app.modules.export.operations.SenderFilteredExport;
import com.vibes.app.modules.messages.repositories.MessageRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for chat export functionality.
 * 
 * Endpoints:
 * - POST /chats/{chatId}/export?format=json|csv
 * - POST /chats/{chatId}/export/range?format=json|csv&startDate=2024-01-01&endDate=2024-12-31
 * - POST /chats/{chatId}/export/senders?format=json|csv&senderIds=user1,user2
 */
@RestController
@RequestMapping("/chats/{chatId}/export")
public class ExportController {

    private final MessageRepository messageRepository;
    private final JsonExportFormatter jsonFormatter;
    private final CsvExportFormatter csvFormatter;

    public ExportController(MessageRepository messageRepository,
                           JsonExportFormatter jsonFormatter,
                           CsvExportFormatter csvFormatter) {
        this.messageRepository = messageRepository;
        this.jsonFormatter = jsonFormatter;
        this.csvFormatter = csvFormatter;
    }

    /**
     * Export full chat history.
     * POST /chats/{chatId}/export?format=json
     */
    @PostMapping
    public ResponseEntity<String> exportChat(
            @PathVariable String chatId,
            @RequestParam(defaultValue = "json") String format) {
        
        ExportFormatter formatter = getFormatter(format);
        FullChatExport export = new FullChatExport(formatter, chatId, messageRepository);
        String result = export.execute();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, formatter.getMimeType())
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"chat-" + chatId + "." + formatter.getFileExtension() + "\"")
                .body(result);
    }

    /**
     * Export messages within a date range.
     * POST /chats/{chatId}/export/range?format=json&startDate=2024-01-01&endDate=2024-12-31
     */
    @PostMapping("/range")
    public ResponseEntity<String> exportDateRange(
            @PathVariable String chatId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "json") String format) {
        
        ExportFormatter formatter = getFormatter(format);
        DateRangeExport export = new DateRangeExport(formatter, chatId, startDate, endDate, messageRepository);
        String result = export.execute();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, formatter.getMimeType())
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"chat-" + chatId + "-" + startDate + "-to-" + endDate + "." + formatter.getFileExtension() + "\"")
                .body(result);
    }

    /**
     * Export messages from specific sender(s).
     * POST /chats/{chatId}/export/senders?format=json&senderIds=user1,user2
     */
    @PostMapping("/senders")
    public ResponseEntity<String> exportBySenders(
            @PathVariable String chatId,
            @RequestParam String senderIds,
            @RequestParam(defaultValue = "json") String format) {
        
        Set<String> senderIdSet = Arrays.stream(senderIds.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        
        ExportFormatter formatter = getFormatter(format);
        SenderFilteredExport export = new SenderFilteredExport(formatter, chatId, senderIdSet, messageRepository);
        String result = export.execute();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, formatter.getMimeType())
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"chat-" + chatId + "-filtered." + formatter.getFileExtension() + "\"")
                .body(result);
    }

    private ExportFormatter getFormatter(String format) {
        return switch (format.toLowerCase()) {
            case "csv" -> csvFormatter;
            default -> jsonFormatter;
        };
    }
}