package com.vibes.app.modules.filesupport.config;

import com.vibes.app.modules.filesupport.decorators.CompressionFileStoreDecorator;
import com.vibes.app.modules.filesupport.decorators.LoggingFileStoreDecorator;
import com.vibes.app.modules.filesupport.factory.*;
import com.vibes.app.modules.filesupport.products.FileStore;
import com.vibes.app.modules.filesupport.products.ProfilePictureStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.provider:s3}")
    private String storageProvider;

    @Bean
    public AbstractStorageFactory storageFactory() {
        AbstractStorageFactory baseFactory;

        switch (storageProvider.toLowerCase()) {
            case "s3":
            case "minio":
                baseFactory = new S3StorageFactory();
//            TODO: Look into adding other providers later on
//            case "gcs":
//                baseFactory = new GCSStorageFactory();
//            case "local":
//                baseFactory = new LocalStorageFactory();
            default:
                baseFactory = new S3StorageFactory();
        }

        AbstractStorageFactory finalBaseFactory = baseFactory;
        return new AbstractStorageFactory() {
            @Override
            public FileStore createFileStore() {
                return new LoggingFileStoreDecorator(
                        new CompressionFileStoreDecorator(
                                finalBaseFactory.createFileStore()
                        )
                );
            }

            @Override
            public ProfilePictureStore createProfilePictureStore() {
                return finalBaseFactory.createProfilePictureStore();
            }
        };
    }
}