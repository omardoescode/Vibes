package com.vibes.app.modules.auth.oauth;

public interface OAuthProvider {

  /**
   * Returns the provider name (e.g., "google", "github")
   */
  String getProviderName();

  /**
   * Builds the authorization URL to redirect user to OAuth provider
   */
  String buildAuthorizationUrl(String state);

  /**
   * Exchanges authorization code for access token and user info
   */
  OAuthUserInfo exchangeCodeForUserInfo(String authorizationCode);
}
