package com.vibes.app.modules.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketManager implements WebSocketMessageBrokerConfigurer {

    private final UserIdHandshakeHandler userIdHandshakeHandler;

    public WebSocketManager(UserIdHandshakeHandler userIdHandshakeHandler) {
        this.userIdHandshakeHandler = userIdHandshakeHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(userIdHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
