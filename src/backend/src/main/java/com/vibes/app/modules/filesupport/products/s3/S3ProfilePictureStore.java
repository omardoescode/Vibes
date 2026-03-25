package com.vibes.app.modules.filesupport.products.s3;
import com.vibes.app.modules.filesupport.products.ProfilePictureStore;
import com.vibes.app.modules.filesupport.adapters.S3Api;
import com.vibes.app.modules.filesupport.adapters.MinioAdapter;

import java.io.InputStream;

public class S3ProfilePictureStore implements ProfilePictureStore {
    private final S3Api s3Api = MinioAdapter.getInstance();

    @Override
    public String uploadProfilePicture(InputStream data, String userId) {
        return s3Api.uploadFile(data, "profile-" + userId);
    }
    @Override
    public String getViewLink(String fileId) {
        return "/filesupport/profiles/view/" + fileId;
    }
    @Override
    public byte[] getProfilePicture(String fileId) {
        return s3Api.downloadFile(fileId);
    }
}