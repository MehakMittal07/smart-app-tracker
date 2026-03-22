package com.tracker.controller;

import com.tracker.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class AuthTestController {

    private final JwtUtil jwtUtil;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/test-jwt")
    public ResponseEntity<Map<String, Object>> testJwt() {
        try {
            String token = jwtUtil.generateToken(999L, "test@test.com");
            return ResponseEntity.ok(Map.of(
                    "status", "JWT_OK",
                    "email",  jwtUtil.extractEmail(token),
                    "userId", jwtUtil.extractUserId(token)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status",  "FAILED",
                    "message", e.getMessage()
            ));
        }
    }
}