package com.vibes.app.modules.auth.dto;

import java.util.UUID;

public class OAuthLoginResponse {

  private UUID id;
  private String username;
  private String email;
  private String profilePictureUrl;

  public OAuthLoginResponse() {}

  public OAuthLoginResponse(UUID id, String username, String email, String profilePictureUrl) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.profilePictureUrl = profilePictureUrl;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getProfilePictureUrl() {
    return profilePictureUrl;
  }

  public void setProfilePictureUrl(String profilePictureUrl) {
    this.profilePictureUrl = profilePictureUrl;
  }
}
