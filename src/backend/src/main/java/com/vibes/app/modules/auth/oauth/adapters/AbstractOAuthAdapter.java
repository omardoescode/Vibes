package com.vibes.app.modules.auth.oauth.adapters;

import com.vibes.app.modules.auth.oauth.OAuthUserInfo;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public abstract class AbstractOAuthAdapter {

  protected final RestTemplate restTemplate = new RestTemplate();

  protected OAuthUserInfo fetchUserInfo(String accessToken, String userInfoUri) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        userInfoUri,
        HttpMethod.GET,
        entity,
        Map.class
    );

    return mapToUserInfo(response.getBody());
  }

  protected abstract OAuthUserInfo mapToUserInfo(Map<String, Object> userInfo);

  protected String exchangeCodeForToken(
      String authorizationCode,
      String tokenUri,
      String clientId,
      String clientSecret,
      String redirectUri
  ) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("code", authorizationCode);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("redirect_uri", redirectUri);

    HttpEntity<MultiValueMap<String, String>> request =
        new HttpEntity<>(body, headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        tokenUri,
        HttpMethod.POST,
        request,
        Map.class
    );

    Map responseBody = response.getBody();
    if (responseBody == null || !responseBody.containsKey("access_token")) {
      throw new RuntimeException("Failed to obtain access token from OAuth provider");
    }

    return (String) responseBody.get("access_token");
  }

  protected String buildAuthorizationUrl(
      String authorizationUri,
      String clientId,
      String redirectUri,
      String scope,
      String state
  ) {
    return UriComponentsBuilder.fromUriString(authorizationUri)
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", redirectUri)
        .queryParam("response_type", "code")
        .queryParam("scope", scope)
        .queryParam("state", state)
        .build()
        .toUriString();
  }
}
