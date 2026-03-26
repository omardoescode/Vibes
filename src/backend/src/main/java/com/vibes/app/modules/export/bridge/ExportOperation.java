package com.vibes.app.modules.export.bridge;

import com.vibes.app.modules.messages.entities.Message;
import java.util.List;

/**
 * Abstraction for Bridge pattern.
 * Defines what to export (the operation).
 * Holds a reference to the ExportFormatter (implementor).
 */
public abstract class ExportOperation {
    protected final ExportFormatter formatter;
    
    public ExportOperation(ExportFormatter formatter) {
        this.formatter = formatter;
    }
    
    /**
     * Execute the export operation.
     * 
     * @return the formatted export data
     */
    public abstract String execute();
    
    /**
     * Get the messages that will be exported.
     * 
     * @return list of messages to export
     */
    protected abstract List<Message> getMessages();
    
    /**
     * Get the formatter being used.
     * 
     * @return the export formatter
     */
    public ExportFormatter getFormatter() {
        return formatter;
    }
}