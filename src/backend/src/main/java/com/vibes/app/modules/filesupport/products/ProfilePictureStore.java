package com.vibes.app.modules.filesupport.products;
import java.io.InputStream;

public interface ProfilePictureStore {
    String uploadProfilePicture(java.io.InputStream data, String userId);
    String getViewLink(String fileId);
    byte[] getProfilePicture(String fileId);
}