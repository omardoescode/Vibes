package com.vibes.app.modules.filesupport.products.s3;
import com.vibes.app.modules.filesupport.products.FileStore;
import com.vibes.app.modules.filesupport.singleton.MinIOClient;
import java.io.InputStream;

public class S3FileStore implements FileStore {
    @Override
    public String upload(InputStream data, String filename) {
        return MinIOClient.getInstance().uploadFile(data, "file-" + filename);
    }
    @Override
    public String getDownloadLink(String fileId) {
        return "/filesupport/files/download/" + fileId;
    }
    @Override
    public byte[] downloadFile(String fileId) {
        return MinIOClient.getInstance().downloadFile(fileId);
    }
}