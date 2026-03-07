package com.vibes.app.modules.messages.factory;

import com.vibes.app.modules.filesupport.factory.AbstractStorageFactory;
import com.vibes.app.modules.filesupport.products.AttachmentStore;
import com.vibes.app.modules.messages.dto.MessagePayload;
import com.vibes.app.modules.messages.entities.AttachmentMessage;
import com.vibes.app.modules.messages.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class AttachmentMessageFactory extends MessageFactory {
    private final AbstractStorageFactory storageFactory;

    @Autowired
    public AttachmentMessageFactory(AbstractStorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }

    @Override
    public Message createMessage(MessagePayload payload) throws Exception {
        AttachmentStore attachmentStore = storageFactory.createAttachmentStore();
        String uniqueId = UUID.randomUUID().toString();

        String fileId = attachmentStore.uploadAttachment(
                payload.getFileContent().getInputStream(),
                uniqueId + "-" + payload.getFileContent().getOriginalFilename()
        );

        String viewLink = attachmentStore.getAttachmentLink(fileId);
        return new AttachmentMessage(viewLink, payload.getSenderId(), payload.getChatId());
    }
}