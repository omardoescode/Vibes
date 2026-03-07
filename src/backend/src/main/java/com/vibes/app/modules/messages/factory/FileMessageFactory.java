package com.vibes.app.modules.messages.factory;

import com.vibes.app.modules.filesupport.factory.AbstractStorageFactory;
import com.vibes.app.modules.filesupport.products.FileStore;
import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.FileMessage;
import com.vibes.app.modules.messages.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class FileMessageFactory extends MessageFactory {
    private final AbstractStorageFactory storageFactory;

    @Autowired
    public FileMessageFactory(AbstractStorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }

    @Override
    public Message createMessage(MessagePayload payload) throws Exception {
        FileStore fileStore = storageFactory.createFileStore();
        String uniqueId = UUID.randomUUID().toString();

        String fileId = fileStore.upload(
                payload.getFileContent().getInputStream(),
                uniqueId + "-" + payload.getFileContent().getOriginalFilename()
        );

        String downloadLink = fileStore.getDownloadLink(fileId);
        return new FileMessage(downloadLink, payload.getSenderId(), payload.getChatId());
    }
}