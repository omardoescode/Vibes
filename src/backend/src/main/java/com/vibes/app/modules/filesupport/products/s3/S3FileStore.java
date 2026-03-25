package com.vibes.app.modules.filesupport.products.s3;

import com.vibes.app.modules.filesupport.products.FileStore;
import com.vibes.app.modules.filesupport.adapters.S3Api;
import com.vibes.app.modules.filesupport.adapters.MinioAdapter;
import java.io.InputStream;

public class S3FileStore implements FileStore {

    private final S3Api s3Api = MinioAdapter.getInstance();

    @Override
    public String upload(InputStream data, String filename) {
        return s3Api.uploadFile(data, "file-" + filename);
    }
    @Override
    public String getDownloadLink(String fileId) {
        return "/filesupport/files/download/" + fileId;
    }
    @Override
    public byte[] downloadFile(String fileId) {
        return s3Api.downloadFile(fileId);
    }
}