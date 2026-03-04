package com.vibes.app.modules.health.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of(
        "status", "UP",
        "service", "vibes-core-backend");
  }
}
