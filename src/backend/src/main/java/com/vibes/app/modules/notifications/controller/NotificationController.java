package com.vibes.app.modules.notifications.controller;

import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import com.vibes.app.modules.notifications.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final UserCredentialsRepository userCredentialsRepository;

  public NotificationController(
      NotificationService notificationService,
      UserCredentialsRepository userCredentialsRepository
  ) {
    this.notificationService = notificationService;
    this.userCredentialsRepository = userCredentialsRepository;
  }

  @PostMapping("/read/{chatId}")
  public ResponseEntity<Void> markAsRead(
      @PathVariable UUID chatId,
      Authentication authentication
  ) {
    UUID userId = getUserIdFromAuth(authentication);
    notificationService.resetUnreadCount(userId, chatId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/unread")
  public ResponseEntity<Map<UUID, Integer>> getUnreadCounts(Authentication authentication) {
    UUID userId = getUserIdFromAuth(authentication);
    Map<UUID, Integer> counts = notificationService.getAllUnreadCounts(userId);
    return ResponseEntity.ok(counts);
  }

  @GetMapping("/unread/{chatId}")
  public ResponseEntity<Integer> getUnreadCountForChat(
      @PathVariable UUID chatId,
      Authentication authentication
  ) {
    UUID userId = getUserIdFromAuth(authentication);
    int count = notificationService.getUnreadCount(userId, chatId);
    return ResponseEntity.ok(count);
  }

  private UUID getUserIdFromAuth(Authentication authentication) {
    String email = authentication.getName();
    return userCredentialsRepository.findByEmail(email)
        .map(credentials -> credentials.getUser().getId())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }
}
