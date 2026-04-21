package com.vibes.app.modules.chat.services;

import com.vibes.app.modules.chat.dto.ChatResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ChatIterator implements Iterator<ChatResponse> {

    private final List<ChatResponse> merged;
    private int cursor = 0;

    public ChatIterator(List<ChatResponse> privateChats,
                        List<ChatResponse> groupChats,
                        Comparator<ChatResponse> comparator) {
        this.merged = new ArrayList<>(privateChats.size() + groupChats.size());
        this.merged.addAll(privateChats);
        this.merged.addAll(groupChats);
        this.merged.sort(comparator);
    }

    @Override
    public boolean hasNext() {
        return cursor < merged.size();
    }

    @Override
    public ChatResponse next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return merged.get(cursor++);
    }
}
