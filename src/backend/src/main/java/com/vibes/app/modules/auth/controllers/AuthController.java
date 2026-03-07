package com.vibes.app.modules.auth.controllers;

import com.vibes.app.modules.auth.dto.LoginRequest;
import com.vibes.app.modules.auth.dto.RegisterRequest;
import com.vibes.app.modules.auth.dto.UserResponse;
import com.vibes.app.modules.auth.services.AuthService;
import com.vibes.app.modules.filesupport.factory.AbstractStorageFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final AbstractStorageFactory storageFactory;

    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          AbstractStorageFactory storageFactory) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.storageFactory = storageFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            UserResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
            UserResponse profile = authService.getProfileByEmail(request.getEmail());
            authService.updateStatus(profile.getId(), "online");
            return ResponseEntity.ok(Map.of("message", "Login successful"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                UserResponse profile = authService.getProfileByEmail(authentication.getName());
                authService.updateStatus(profile.getId(), "offline");
            } catch (Exception ignored) {
                // best-effort: don't fail logout if status update errors
            }
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        try {
            // authentication.getName() is the email (set by UserDetailsServiceImpl)
            UserResponse profile = authService.getProfileByEmail(authentication.getName());
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
    }

    /**
     * Upload or replace the authenticated user's profile picture.
     * POST /auth/me/avatar  (multipart/form-data, field name: "file")
     * Returns updated UserResponse.
     */
    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        try {
            // Use the user's UUID as the file key — email contains @ which breaks URL paths
            UserResponse currentUser = authService.getProfileByEmail(authentication.getName());
            String fileId = storageFactory.createProfilePictureStore()
                    .uploadProfilePicture(file.getInputStream(), currentUser.getId().toString());
            String viewUrl = storageFactory.createProfilePictureStore().getViewLink(fileId);
            UserResponse updated = authService.updateProfilePicture(authentication.getName(), viewUrl);
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to read uploaded file"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(authService.searchUsers(query));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(authService.getProfile(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

