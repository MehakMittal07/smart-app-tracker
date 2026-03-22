package com.tracker.controller;

import com.tracker.exception.TokenExpiredException;
import com.tracker.model.Opportunity;
import com.tracker.service.GmailService;
import com.tracker.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.ErrorManager;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
public class GmailController {

    private final GmailService gmailService;
    private final JwtUtil jwtUtil;

    // POST http://localhost:8080/api/gmail/sync
    // Header: Authorization: Bearer <your_jwt>

    @PostMapping("/sync")
    public ResponseEntity<?> sync(Authentication auth) {
        try {
            Long userId = (Long) auth.getDetails();
            List<Opportunity> results = gmailService.fetchAndParseEmails(userId);
            return ResponseEntity.ok(Map.of(
                    "status",  "success",
                    "found",   results.size(),
                    "message", results.size() + " new opportunities synced"
            ));
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status",  "token_expired",
                    "message", e.getMessage(),
                    "action",  "please_relogin"
            ));
        } catch (Exception e) {


            return ResponseEntity.status(500).body(Map.of(
                    "status",  "error",
                    "message", e.getMessage()
            ));
        }
    }
}