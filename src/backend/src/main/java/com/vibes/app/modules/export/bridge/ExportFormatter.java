package com.vibes.app.modules.export.bridge;

import com.vibes.app.modules.messages.entities.Message;
import java.util.List;

/**
 * Implementor interface for Bridge pattern.
 * Defines how to format exported data.
 */
public interface ExportFormatter {
    /**
     * Format a list of messages into the target export format.
     * 
     * @param messages the messages to format
     * @return formatted string representation
     */
    String format(List<Message> messages);
    
    /**
     * Get the MIME type for this format (e.g., "application/json", "text/csv").
     * 
     * @return the MIME type
     */
    String getMimeType();
    
    /**
     * Get the file extension for this format (e.g., "json", "csv").
     * 
     * @return the file extension
     */
    String getFileExtension();
}