package com.tracker.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.tracker.exception.TokenExpiredException;
import com.tracker.model.Opportunity;
import com.tracker.model.User;
import com.tracker.repository.OpportunityRepository;
import com.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {

    private final UserRepository        userRepository;
    private final OpportunityRepository opportunityRepository;
    private final EmailParserService    emailParserService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private static final String APP_NAME   = "SmartAppTracker";
    private static final int    MAX_EMAILS = 50;

    public List<Opportunity> fetchAndParseEmails(Long userId) {
        log.info("=== fetchAndParseEmails userId={} ===", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String refreshToken = user.getGoogleRefreshToken();

        // ── Validate token exists ──────────────────────────────────────────
        if (refreshToken == null || refreshToken.isBlank()) {
            clearTokenAndThrow(user, "No refresh token stored. Please log in again.");
        }

        if (refreshToken.startsWith("ya29.")) {
            clearTokenAndThrow(user,
                    "Stored token is an access token, not a refresh token. Please log in again.");
        }

        log.info("Token preview: {}...",
                refreshToken.substring(0, Math.min(15, refreshToken.length())));

        try {
            Gmail client = buildGmailClient(refreshToken);
            return processEmails(client, user);

        } catch (TokenExpiredException e) {
            // Clear the bad token so user is prompted to re-authorize
            clearTokenAndThrow(user, e.getMessage());
            return List.of(); // unreachable but satisfies compiler

        } catch (Exception e) {
            log.error("Gmail sync failed: {}", e.getMessage(), e);
            throw new RuntimeException("Gmail sync failed: " + e.getMessage());
        }
    }

    private Gmail buildGmailClient(String refreshToken)
            throws GeneralSecurityException, IOException {

        try {
            UserCredentials credentials = UserCredentials.newBuilder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRefreshToken(refreshToken)
                    .build();

            credentials.refresh(); // ← will throw IOException if token is invalid
            log.info("Google token refreshed successfully");

            return new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName(APP_NAME).build();

        } catch (IOException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";

            if (msg.contains("invalid_grant") || msg.contains("400")) {
                log.error("Refresh token is invalid/expired: {}", msg);
                // Wrap in our custom exception so caller can clear the token
                throw new TokenExpiredException(
                        "Gmail access has expired (invalid_grant). " +
                                "Please log out and log in again to re-authorize Gmail access.");
            }
            throw e; // rethrow other IO errors
        }
    }

    // ── Clear the bad token from DB and throw TokenExpiredException ────────
    private void clearTokenAndThrow(User user, String message) {
        log.warn("Clearing invalid token for user: {}", user.getEmail());
        user.setGoogleRefreshToken(null);
        userRepository.save(user);
        throw new TokenExpiredException(message);
    }

    private List<Opportunity> processEmails(Gmail client, User user)
            throws IOException {

        List<Opportunity> saved = new ArrayList<>();

        String query = "subject:(internship OR certification OR apply OR deadline " +
                "OR opportunity OR fellowship OR hackathon OR \"last date\")";

        ListMessagesResponse resp = client.users()
                .messages()
                .list("me")
                .setQ(query)
                .setMaxResults((long) MAX_EMAILS)
                .execute();

        if (resp.getMessages() == null || resp.getMessages().isEmpty()) {
            log.info("No matching emails found for {}", user.getEmail());
            return saved;
        }

        log.info("Found {} candidate emails", resp.getMessages().size());

        for (Message msg : resp.getMessages()) {
            try {
                if (opportunityRepository.existsBySourceEmailId(msg.getId())) continue;

                Message full = client.users()
                        .messages()
                        .get("me", msg.getId())
                        .setFormat("full")
                        .execute();

                String subject = extractHeader(full, "Subject");
                String body    = extractBody(full);

                Optional<Opportunity> parsed =
                        emailParserService.parse(subject, body, msg.getId(), user);

                parsed.ifPresent(opp -> {
                    opportunityRepository.save(opp);
                    saved.add(opp);
                    log.info("Saved: {}", opp.getTitle());
                });

            } catch (IOException e) {
                log.error("Failed to process message {}: {}", msg.getId(), e.getMessage());
            }
        }

        log.info("Saved {} new opportunities", saved.size());
        return saved;
    }

    private String extractHeader(Message message, String name) {
        return message.getPayload().getHeaders().stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(h -> h.getValue())
                .orElse("");
    }

    private String extractBody(Message message) {
        var payload = message.getPayload();

        if (payload.getBody() != null && payload.getBody().getData() != null) {
            return decode(payload.getBody().getData());
        }

        if (payload.getParts() != null) {
            String plain = "", html = "";
            for (var part : payload.getParts()) {
                if (part.getBody() == null || part.getBody().getData() == null) continue;
                if ("text/plain".equals(part.getMimeType())) plain = decode(part.getBody().getData());
                if ("text/html".equals(part.getMimeType()))  html  = decode(part.getBody().getData());
            }
            return plain.isEmpty() ? stripHtml(html) : plain;
        }
        return "";
    }

    private String decode(String data) {
        try {
            return new String(Base64.getUrlDecoder().decode(data),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { return ""; }
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s{2,}", " ").trim();
    }
}