package com.vibes.app.modules.auth.services;

import com.vibes.app.modules.auth.dto.OAuthLoginResponse;
import com.vibes.app.modules.auth.models.OauthAccounts;
import com.vibes.app.modules.auth.models.User;
import com.vibes.app.modules.auth.models.UserCredentials;
import com.vibes.app.modules.auth.oauth.OAuthProvider;
import com.vibes.app.modules.auth.oauth.OAuthProviderRegistry;
import com.vibes.app.modules.auth.oauth.OAuthUserInfo;
import com.vibes.app.modules.auth.repositories.OauthAccountsRepository;
import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import com.vibes.app.modules.auth.repositories.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuthService {

  private final OAuthProviderRegistry providerRegistry;
  private final UserRepository userRepository;
  private final UserCredentialsRepository userCredentialsRepository;
  private final OauthAccountsRepository oauthAccountsRepository;

  public OAuthService(
      OAuthProviderRegistry providerRegistry,
      UserRepository userRepository,
      UserCredentialsRepository userCredentialsRepository,
      OauthAccountsRepository oauthAccountsRepository
  ) {
    this.providerRegistry = providerRegistry;
    this.userRepository = userRepository;
    this.userCredentialsRepository = userCredentialsRepository;
    this.oauthAccountsRepository = oauthAccountsRepository;
  }

  @Transactional
  public OAuthLoginResponse handleOAuthLogin(String providerName, String authorizationCode) {
    OAuthProvider provider = providerRegistry.getProvider(providerName);
    OAuthUserInfo oauthInfo = provider.exchangeCodeForUserInfo(authorizationCode);

    Optional<OauthAccounts> existingAccount = oauthAccountsRepository
        .findByProviderAndProviderUserId(providerName, oauthInfo.providerUserId());

    if (existingAccount.isPresent()) {
      User user = existingAccount.get().getUser();
      user.setStatus("online");
      userRepository.save(user);
      return createSessionForUser(user);
    }

    Optional<UserCredentials> existingCredentials = userCredentialsRepository.findByEmail(oauthInfo.email());

    if (existingCredentials.isPresent()) {
      User user = existingCredentials.get().getUser();
      linkOAuthToUser(user, providerName, oauthInfo);
      user.setStatus("online");
      userRepository.save(user);
      return createSessionForUser(user);
    }

    User newUser = createUserFromOAuth(oauthInfo);
    linkOAuthToUser(newUser, providerName, oauthInfo);
    return createSessionForUser(newUser);
  }

  @Transactional
  public void linkOAuthToExistingUser(User user, String providerName, String authCode) {
    OAuthProvider provider = providerRegistry.getProvider(providerName);
    OAuthUserInfo oauthInfo = provider.exchangeCodeForUserInfo(authCode);

    Optional<UserCredentials> userCredentials = userCredentialsRepository.findByUserId(user.getId());
    if (userCredentials.isPresent() && !userCredentials.get().getEmail().equals(oauthInfo.email())) {
      throw new IllegalArgumentException("OAuth email does not match user email");
    }

    if (oauthAccountsRepository.existsByProviderAndProviderUserId(providerName, oauthInfo.providerUserId())) {
      throw new IllegalArgumentException("This OAuth account is already linked to another user");
    }

    linkOAuthToUser(user, providerName, oauthInfo);
  }

  private User createUserFromOAuth(OAuthUserInfo oauthInfo) {
    User user = new User();
    user.setUsername(generateUsername(oauthInfo.name()));
    user.setProfilePictureUrl(oauthInfo.pictureUrl());
    user.setStatus("online");

    User savedUser = userRepository.save(user);

    UserCredentials credentials = new UserCredentials();
    credentials.setEmail(oauthInfo.email());
    credentials.setPasswordHash("");
    credentials.setUser(savedUser);
    userCredentialsRepository.save(credentials);

    return savedUser;
  }

  private void linkOAuthToUser(User user, String providerName, OAuthUserInfo oauthInfo) {
    OauthAccounts account = new OauthAccounts();
    account.setProvider(providerName);
    account.setProviderUserId(oauthInfo.providerUserId());
    account.setUser(user);

    oauthAccountsRepository.save(account);
  }

  private OAuthLoginResponse createSessionForUser(User user) {
    Optional<UserCredentials> credentials = userCredentialsRepository.findByUserId(user.getId());
    String email = credentials.map(UserCredentials::getEmail).orElse("");

    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        email,
        null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);

    return new OAuthLoginResponse(
        user.getId(),
        user.getUsername(),
        email,
        user.getProfilePictureUrl()
    );
  }

  private String generateUsername(String name) {
    String baseUsername = name.toLowerCase().replaceAll("[^a-z0-9]", "");
    String username = baseUsername;
    int suffix = 1;

    while (userRepository.existsByUsername(username)) {
      username = baseUsername + suffix;
      suffix++;
    }

    return username;
  }
}
