package com.vibes.app.modules.filesupport.products;
import java.io.InputStream;

public interface FileStore {
    String upload(java.io.InputStream data, String filename);
    String getDownloadLink(String fileId);
    byte[] downloadFile(String fileId);
}