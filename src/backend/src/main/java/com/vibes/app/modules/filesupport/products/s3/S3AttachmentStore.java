package com.vibes.app.modules.filesupport.products.s3;
import com.vibes.app.modules.filesupport.products.AttachmentStore;
import com.vibes.app.modules.filesupport.singleton.MinIOClient;
import java.io.InputStream;

public class S3AttachmentStore implements AttachmentStore {
    @Override
    public String uploadAttachment(InputStream data, String messageId) {
        return MinIOClient.getInstance().uploadFile(data, "attachment-" + messageId);
    }
    @Override
    public String getAttachmentLink(String fileId) {
        return "/filesupport/attachments/view/" + fileId;
    }
    @Override
    public byte[] getAttachment(String fileId) {
        return MinIOClient.getInstance().downloadFile(fileId);
    }
}