package com.vibes.app.modules.auth.dto;

public class OAuthLoginRequest {

  private String authorizationCode;

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }
}
