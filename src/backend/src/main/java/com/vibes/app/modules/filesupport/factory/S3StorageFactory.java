package com.vibes.app.modules.filesupport.factory;
import com.vibes.app.modules.filesupport.products.*;
import com.vibes.app.modules.filesupport.products.s3.*;

public class S3StorageFactory implements AbstractStorageFactory {
    @Override
    public FileStore createFileStore() { return new S3FileStore(); }
    @Override
    public ProfilePictureStore createProfilePictureStore() { return new S3ProfilePictureStore(); }
    @Override
    public AttachmentStore createAttachmentStore() { return new S3AttachmentStore(); }
}