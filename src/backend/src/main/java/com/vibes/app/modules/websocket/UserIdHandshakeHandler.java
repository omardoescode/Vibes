package com.vibes.app.modules.websocket;

import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Replaces the default WebSocket principal at HTTP upgrade time.
 *
 * During the SockJS/WebSocket handshake, the HTTP request carries the
 * authenticated session (JSESSIONID cookie). At this point the servlet
 * request has a fully-resolved Principal whose name is the user's email.
 *
 * We look up the UUID here and return a Principal whose name IS the UUID.
 * This principal is stored on the WebSocket session and is available on
 * every subsequent STOMP frame — including SEND — as the `Principal`
 * parameter in @MessageMapping handlers.
 *
 * This means convertAndSendToUser(uuid, "/queue/messages", msg) correctly
 * routes to the right session for both sender and recipient.
 */
@Component
public class UserIdHandshakeHandler extends DefaultHandshakeHandler {

    private static final Logger log = LoggerFactory.getLogger(UserIdHandshakeHandler.class);

    private final UserCredentialsRepository userCredentialsRepository;

    public UserIdHandshakeHandler(UserCredentialsRepository userCredentialsRepository) {
        this.userCredentialsRepository = userCredentialsRepository;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // Grab the authenticated principal from the underlying HTTP request
        Principal httpPrincipal = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            httpPrincipal = servletRequest.getServletRequest().getUserPrincipal();
        }

        if (httpPrincipal == null) {
            log.warn("[ws] handshake has no HTTP principal — unauthenticated connection");
            return null;
        }

        String email = httpPrincipal.getName();
        return userCredentialsRepository.findByEmail(email)
                .map(credentials -> {
                    String userId = credentials.getUser().getId().toString();
                    log.info("[ws] handshake principal {} → {}", email, userId);
                    return (Principal) () -> userId;
                })
                .orElseGet(() -> {
                    log.warn("[ws] no user found for email {}", email);
                    return null;
                });
    }
}
