package com.vibes.app.modules.filesupport.config;

import com.vibes.app.modules.filesupport.factory.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.provider:local}")
    private String storageProvider;

    @Bean
    public AbstractStorageFactory storageFactory() {
        switch (storageProvider.toLowerCase()) {
            case "s3":
            case "minio":
                return new S3StorageFactory();
//            TODO: Look into adding other providers later on
//            case "gcs":
//                return new GCSStorageFactory();
//            case "local":
//                return new LocalStorageFactory();
            default:
                return new S3StorageFactory();
        }
    }
}