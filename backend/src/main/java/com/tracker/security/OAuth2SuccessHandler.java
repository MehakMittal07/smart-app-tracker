package com.tracker.security;

import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import com.tracker.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("=== OAuth2SuccessHandler ===");
        try {
            OAuth2AuthenticationToken token =
                    (OAuth2AuthenticationToken) authentication;

            String email          = token.getPrincipal().getAttribute("email");
            String registrationId = token.getAuthorizedClientRegistrationId();
            String principalName  = token.getName();

            log.info("email={}", email);

            // Get refresh token from authorized client
            OAuth2AuthorizedClient client =
                    authorizedClientService.loadAuthorizedClient(
                            registrationId, principalName);

            if (client != null && client.getRefreshToken() != null) {
                String refreshToken = client.getRefreshToken().getTokenValue();
                log.info("Refresh token obtained: {}...",
                        refreshToken.substring(0, Math.min(20, refreshToken.length())));

                userRepository.findByEmail(email).ifPresent(u -> {
                    u.setGoogleRefreshToken(refreshToken);
                    userRepository.save(u);
                    log.info("Refresh token saved to DB");
                });
            } else {
                log.warn("No refresh token in authorizedClient — " +
                        "client={}", client != null ? "present" : "null");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String jwt = jwtUtil.generateToken(user.getId(), user.getEmail());
            response.sendRedirect(frontendUrl + "/auth/callback?token=" + jwt);

        } catch (Exception e) {
            log.error("SuccessHandler failed: {}", e.getMessage(), e);
            response.sendRedirect(frontendUrl + "/login?error=server_error");
        }
    }
}