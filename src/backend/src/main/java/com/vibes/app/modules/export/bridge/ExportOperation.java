package com.vibes.app.modules.export.bridge;

import com.vibes.app.modules.messages.entities.Message;
import java.util.List;

public abstract class ExportOperation {
    protected final ExportFormatter formatter;
    
    public ExportOperation(ExportFormatter formatter) {
        this.formatter = formatter;
    }
    
    public final String execute() {
        return formatter.format(getMessages());
    }

    protected abstract List<Message> getMessages();
    
    public ExportFormatter getFormatter() {
        return formatter;
    }
}