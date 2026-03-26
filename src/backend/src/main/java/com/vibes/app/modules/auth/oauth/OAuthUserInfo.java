package com.vibes.app.modules.auth.oauth;

public record OAuthUserInfo(
    String providerUserId,
    String email,
    String name,
    String pictureUrl,
    boolean emailVerified
) {
}
