package com.vibes.app.modules.auth.oauth;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OAuthProviderRegistry {

  private final Map<String, OAuthProvider> providers = new HashMap<>();

  public OAuthProviderRegistry(List<OAuthProvider> providerList) {
    for (OAuthProvider provider : providerList) {
      providers.put(provider.getProviderName(), provider);
    }
  }

  public OAuthProvider getProvider(String name) {
    OAuthProvider provider = providers.get(name);
    if (provider == null) {
      throw new IllegalArgumentException("Unknown OAuth provider: " + name);
    }
    return provider;
  }

  public boolean isSupported(String name) {
    return providers.containsKey(name);
  }
}
