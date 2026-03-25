package com.vibes.app.modules.filesupport.decorators;

import com.vibes.app.modules.filesupport.products.FileStore;
import java.io.InputStream;

public abstract class FileStoreDecorator implements FileStore {
    protected final FileStore wrapper;

    public FileStoreDecorator(FileStore wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public String upload(InputStream data, String filename) {
        return wrapper.upload(data, filename);
    }

    @Override
    public String getDownloadLink(String fileId) {
        return wrapper.getDownloadLink(fileId);
    }

    @Override
    public byte[] downloadFile(String fileId) {
        return wrapper.downloadFile(fileId);
    }
}