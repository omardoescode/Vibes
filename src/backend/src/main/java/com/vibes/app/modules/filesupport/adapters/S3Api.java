package com.vibes.app.modules.filesupport.adapters;

import java.io.InputStream;

public interface S3Api {
    String uploadFile(InputStream fileStream, String objectName);
    byte[] downloadFile(String objectName);
    boolean isHealthy();
}