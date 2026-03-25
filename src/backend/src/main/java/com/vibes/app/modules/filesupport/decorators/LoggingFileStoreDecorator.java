package com.vibes.app.modules.filesupport.decorators;

import com.vibes.app.modules.filesupport.products.FileStore;
import java.io.InputStream;

public class LoggingFileStoreDecorator extends FileStoreDecorator {

    public LoggingFileStoreDecorator(FileStore wrapper) {
        super(wrapper);
    }

    @Override
    public String upload(InputStream data, String filename) {
        System.out.println("[LOG] Starting upload for file: " + filename);
        long startTime = System.currentTimeMillis();

        String fileId = super.upload(data, filename);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[LOG] Successfully uploaded " + filename + " in " + duration + "ms. Assigned ID: " + fileId);

        return fileId;
    }

    @Override
    public byte[] downloadFile(String fileId) {
        System.out.println("[LOG] Starting download for file ID: " + fileId);
        long startTime = System.currentTimeMillis();

        byte[] result = super.downloadFile(fileId);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[LOG] Successfully downloaded " + fileId + " (" + result.length + " bytes) in " + duration + "ms.");

        return result;
    }
}