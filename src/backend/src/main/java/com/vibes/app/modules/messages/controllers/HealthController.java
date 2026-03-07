package com.vibes.app.modules.messages.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController("messagesHealthController")
@RequestMapping("/messages")
public class HealthController {

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of(
            "status", "UP",
            "service", "vibes-messages"
    );
  }
}