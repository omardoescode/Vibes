package com.vibes.app.modules.messages.services;

import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.Message;
import com.vibes.app.modules.messages.factory.FileMessageFactory;
import com.vibes.app.modules.messages.factory.TextMessageFactory;
import com.vibes.app.modules.messages.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository repository;
    private final TextMessageFactory textFactory;
    private final FileMessageFactory fileFactory;

    @Autowired
    public MessageService(MessageRepository repository, TextMessageFactory textFactory,
                          FileMessageFactory fileFactory) {
        this.repository = repository;
        this.textFactory = textFactory;
        this.fileFactory = fileFactory;
    }

    public Message processAndSaveTextMessage(MessagePayload payload) throws Exception {
        Message message = textFactory.createMessage(payload);
        return repository.save(message);
    }

    public Message processAndSaveMediaMessage(MessagePayload payload) throws Exception {
        Message message = fileFactory.createMessage(payload);
        return repository.save(message);
    }

    public Iterable<Message> getMessagesByChatId(String chatId) {
        return repository.findByChatIdOrderByTimestampAsc(chatId);
    }
}