package com.vibes.app.modules.auth.oauth.adapters;

import com.vibes.app.modules.auth.oauth.OAuthProvider;
import com.vibes.app.modules.auth.oauth.OAuthUserInfo;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

/**
 * Adapter that wraps Spring Security OAuth2 client library and adapts it
 * to the application's OAuthProvider interface.
 * 
 * Target: OAuthProvider (application interface)
 * Adaptee: OAuth2AuthorizedClientManager (Spring Security library)
 * Adapter: This class - translates OAuthProvider calls to Spring Security calls
 */
@Component
public class SpringSecurityOAuthAdapter implements OAuthProvider {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestTemplate restTemplate;
    private final String providerName = "github";

    public SpringSecurityOAuthAdapter(
            OAuth2AuthorizedClientManager authorizedClientManager,
            ClientRegistrationRepository clientRegistrationRepository) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(providerName);
        if (registration == null) {
            throw new IllegalArgumentException("Unknown OAuth provider: " + providerName);
        }

        return registration.getProviderDetails().getAuthorizationUri() +
                "?" + OAuth2ParameterNames.CLIENT_ID + "=" + registration.getClientId() +
                "&" + OAuth2ParameterNames.REDIRECT_URI + "=" + registration.getRedirectUri() +
                "&" + OAuth2ParameterNames.RESPONSE_TYPE + "=code" +
                "&" + OAuth2ParameterNames.SCOPE + "=" + String.join(" ", registration.getScopes()) +
                "&" + OAuth2ParameterNames.STATE + "=" + state;
    }

    @Override
    public OAuthUserInfo exchangeCodeForUserInfo(String authorizationCode) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(providerName);
        if (registration == null) {
            throw new IllegalArgumentException("Unknown OAuth provider: " + providerName);
        }

        // Create a temporary authorized client using the authorization code
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(providerName)
                .principal(UUID.randomUUID().toString()) // Temporary principal
                .attribute(OAuth2AuthorizationExchange.class.getName(), 
                    new OAuth2AuthorizationExchange(
                        null, // authorization request - not needed for token exchange
                        null  // authorization response - not needed for token exchange
                    ))
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        
        if (authorizedClient == null) {
            throw new RuntimeException("Failed to authorize with " + providerName);
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // Fetch user info from provider
        String userInfoUri = registration.getProviderDetails().getUserInfoEndpoint().getUri();
        return fetchUserInfo(accessToken, userInfoUri, providerName);
    }

    private OAuthUserInfo fetchUserInfo(String accessToken, String userInfoUri, String provider) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userInfo = response.getBody();
        if (userInfo == null) {
            throw new RuntimeException("Failed to fetch user info from " + provider);
        }

        // Map provider-specific response to OAuthUserInfo
        return mapToUserInfo(userInfo, provider);
    }

    private OAuthUserInfo mapToUserInfo(Map<String, Object> userInfo, String provider) {
        switch (provider.toLowerCase()) {
            case "github":
                return new OAuthUserInfo(
                        String.valueOf(userInfo.get("id")),
                        (String) userInfo.get("email"),
                        (String) userInfo.get("name"),
                        (String) userInfo.get("avatar_url"),
                        true
                );
            case "google":
                return new OAuthUserInfo(
                        (String) userInfo.get("sub"),
                        (String) userInfo.get("email"),
                        (String) userInfo.get("name"),
                        (String) userInfo.get("picture"),
                        true
                );
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}