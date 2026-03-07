package com.vibes.app.modules.filesupport.controllers;

import com.vibes.app.modules.filesupport.singleton.MinIOClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController("fileSupportHealthController")
@RequestMapping("/filesupport")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        boolean isMinioUp = false;
        try {
            isMinioUp = MinIOClient.getInstance().isHealthy();
        } catch (Exception e) {
            isMinioUp = false;
        }

        return Map.of(
                "status", isMinioUp ? "UP" : "DOWN",
                "service", "vibes-file-support",
                "minio_connection", isMinioUp ? "SUCCESS" : "FAILED"
        );
    }
}