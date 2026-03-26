package com.vibes.app.modules.auth.controllers;

import com.vibes.app.modules.auth.dto.LinkOAuthRequest;
import com.vibes.app.modules.auth.dto.OAuthLoginRequest;
import com.vibes.app.modules.auth.dto.OAuthLoginResponse;
import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.oauth.OAuthProviderRegistry;
import com.vibes.app.modules.auth.services.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/auth/oauth")
public class OAuthController {

  private final OAuthService oauthService;
  private final OAuthProviderRegistry providerRegistry;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  public OAuthController(OAuthService oauthService, OAuthProviderRegistry providerRegistry) {
    this.oauthService = oauthService;
    this.providerRegistry = providerRegistry;
  }

  @GetMapping("/{provider}/authorize")
  public ResponseEntity<Void> authorize(
      @PathVariable String provider,
      @RequestParam(required = false) String state
  ) {
    if (!providerRegistry.isSupported(provider)) {
      return ResponseEntity.badRequest().build();
    }

    String authorizationUrl = providerRegistry.getProvider(provider)
        .buildAuthorizationUrl(state != null ? state : generateState());

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(authorizationUrl))
        .build();
  }

  @GetMapping("/{provider}/callback")
  public ResponseEntity<Void> callback(
      @PathVariable String provider,
      @RequestParam String code,
      @RequestParam(required = false) String state,
      @RequestParam(required = false) String error,
      HttpServletRequest httpRequest
  ) {
    if (error != null) {
      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create(frontendUrl + "/login?error=access_denied"))
          .build();
    }

    try {
      OAuthLoginResponse response = oauthService.handleOAuthLogin(provider, code);
      
      // Store SecurityContext in session (same as regular login)
      HttpSession session = httpRequest.getSession(true);
      SecurityContext sc = SecurityContextHolder.getContext();
      session.setAttribute("SPRING_SECURITY_CONTEXT", sc);

      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create(frontendUrl + "/oauth/success"))
          .build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create(frontendUrl + "/login?error=oauth_failed"))
          .build();
    }
  }

  @PostMapping("/{provider}/login")
  public ResponseEntity<OAuthLoginResponse> login(
      @PathVariable String provider,
      @RequestBody OAuthLoginRequest request
  ) {
    if (!providerRegistry.isSupported(provider)) {
      return ResponseEntity.badRequest().build();
    }

    try {
      OAuthLoginResponse response = oauthService.handleOAuthLogin(provider, request.getAuthorizationCode());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/{provider}/link")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> linkOAuthAccount(
      @PathVariable String provider,
      @RequestBody LinkOAuthRequest request,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    if (!providerRegistry.isSupported(provider)) {
      return ResponseEntity.badRequest().build();
    }

    try {
      User user = new User();
      user.setUsername(userDetails.getUsername());
      oauthService.linkOAuthToExistingUser(user, provider, request.getAuthorizationCode());
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  private String generateState() {
    return UUID.randomUUID().toString();
  }
}
