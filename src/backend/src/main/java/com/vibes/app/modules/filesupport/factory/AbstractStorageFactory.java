package com.vibes.app.modules.filesupport.factory;
import com.vibes.app.modules.filesupport.products.*;

public interface AbstractStorageFactory {
    FileStore createFileStore();
    ProfilePictureStore createProfilePictureStore();
    AttachmentStore createAttachmentStore();
}