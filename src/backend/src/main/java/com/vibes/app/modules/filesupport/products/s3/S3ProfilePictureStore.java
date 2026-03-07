package com.vibes.app.modules.filesupport.products.s3;
import com.vibes.app.modules.filesupport.products.ProfilePictureStore;
import com.vibes.app.modules.filesupport.singleton.MinIOClient;
import java.io.InputStream;

public class S3ProfilePictureStore implements ProfilePictureStore {
    @Override
    public String uploadProfilePicture(InputStream data, String userId) {
        return MinIOClient.getInstance().uploadFile(data, "profile-" + userId);
    }
    @Override
    public String getViewLink(String fileId) {
        return "/filesupport/profiles/view/" + fileId;
    }
    @Override
    public byte[] getProfilePicture(String fileId) {
        return MinIOClient.getInstance().downloadFile(fileId);
    }
}