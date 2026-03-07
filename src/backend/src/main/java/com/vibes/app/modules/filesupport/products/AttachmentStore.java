package com.vibes.app.modules.filesupport.products;
import java.io.InputStream;

public interface AttachmentStore {
    String uploadAttachment(java.io.InputStream data, String messageId);
    String getAttachmentLink(String fileId);
    byte[] getAttachment(String fileId);
}