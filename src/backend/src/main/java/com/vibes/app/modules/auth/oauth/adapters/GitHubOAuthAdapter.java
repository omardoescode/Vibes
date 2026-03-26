package com.vibes.app.modules.auth.oauth.adapters;

import com.vibes.app.modules.auth.oauth.OAuthProvider;
import com.vibes.app.modules.auth.oauth.OAuthUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
public class GitHubOAuthAdapter implements OAuthProvider {

  @Value("${oauth.github.client-id}")
  private String clientId;

  @Value("${oauth.github.client-secret}")
  private String clientSecret;

  @Value("${oauth.github.redirect-uri}")
  private String redirectUri;

  @Value("${oauth.github.authorization-uri}")
  private String authorizationUri;

  @Value("${oauth.github.token-uri}")
  private String tokenUri;

  @Value("${oauth.github.user-info-uri}")
  private String userInfoUri;

  @Value("${oauth.github.scope}")
  private String scope;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public String getProviderName() {
    return "github";
  }

  @Override
  public String buildAuthorizationUrl(String state) {
    return UriComponentsBuilder.fromUriString(authorizationUri)
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", redirectUri)
        .queryParam("scope", scope)
        .queryParam("state", state)
        .build()
        .toUriString();
  }

  @Override
  public OAuthUserInfo exchangeCodeForUserInfo(String authorizationCode) {
    String accessToken = exchangeCodeForToken(authorizationCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.set("Accept", "application/vnd.github+json");
    headers.set("X-GitHub-Api-Version", "2022-11-28");
    HttpEntity<String> entity = new HttpEntity<>(headers);

    // Fetch user info
    ResponseEntity<Map> userResponse = restTemplate.exchange(
        userInfoUri,
        HttpMethod.GET,
        entity,
        Map.class
    );

    Map<String, Object> userInfo = userResponse.getBody();
    if (userInfo == null) {
      throw new RuntimeException("Failed to fetch user info from GitHub");
    }

    // GitHub doesn't return email in the main user info endpoint
    String email = fetchPrimaryEmail(accessToken);
    if (email == null) {
      email = (String) userInfo.get("email");
    }

    return new OAuthUserInfo(
        String.valueOf(userInfo.get("id")),
        email,
        (String) userInfo.get("name"),
        (String) userInfo.get("avatar_url"),
        true
    );
  }

  private String exchangeCodeForToken(String authorizationCode) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("Accept", "application/json");

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
      throw new RuntimeException("Failed to obtain access token from GitHub");
    }

    return (String) responseBody.get("access_token");
  }

  private String fetchPrimaryEmail(String accessToken) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      headers.set("Accept", "application/vnd.github+json");
      headers.set("X-GitHub-Api-Version", "2022-11-28");
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<List> response = restTemplate.exchange(
          "https://api.github.com/user/emails",
          HttpMethod.GET,
          entity,
          List.class
      );

      List<Map<String, Object>> emails = response.getBody();
      if (emails != null) {
        for (Map<String, Object> emailInfo : emails) {
          Boolean primary = (Boolean) emailInfo.get("primary");
          Boolean verified = (Boolean) emailInfo.get("verified");
          if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
            return (String) emailInfo.get("email");
          }
        }
      }
    } catch (Exception e) {
      // Ignore and return null, fallback to public email
    }
    return null;
  }
}
