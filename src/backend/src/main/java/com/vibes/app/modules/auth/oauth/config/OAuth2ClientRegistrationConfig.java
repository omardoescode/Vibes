package com.vibes.app.modules.auth.oauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2ClientRegistrationConfig {

    @Value("${oauth.github.client-id:}")
    private String githubClientId;

    @Value("${oauth.github.client-secret:}")
    private String githubClientSecret;

    @Value("${oauth.github.redirect-uri:http://localhost:8080/auth/oauth/github/callback}")
    private String githubRedirectUri;

    @Value("${oauth.github.authorization-uri:https://github.com/login/oauth/authorize}")
    private String githubAuthorizationUri;

    @Value("${oauth.github.token-uri:https://github.com/login/oauth/access_token}")
    private String githubTokenUri;

    @Value("${oauth.github.user-info-uri:https://api.github.com/user}")
    private String githubUserInfoUri;

    @Value("${oauth.github.scope:user:email,read:user}")
    private String githubScope;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration githubRegistration = ClientRegistration
                .withRegistrationId("github")
                .clientId(githubClientId)
                .clientSecret(githubClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(githubRedirectUri)
                .scope(githubScope.split(","))
                .authorizationUri(githubAuthorizationUri)
                .tokenUri(githubTokenUri)
                .userInfoUri(githubUserInfoUri)
                .userNameAttributeName("id")
                .clientName("GitHub")
                .build();

        return new InMemoryClientRegistrationRepository(githubRegistration);
    }
}