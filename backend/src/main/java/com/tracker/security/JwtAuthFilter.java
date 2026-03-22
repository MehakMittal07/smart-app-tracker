package com.tracker.security;

import com.tracker.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/error")
                || path.startsWith("/api/public/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path  = request.getRequestURI();
        String token = extractToken(request);

        log.debug("JWT filter: path={}, tokenPresent={}", path, token != null);

        if (StringUtils.hasText(token)) {
            if (jwtUtil.isValid(token)) {
                String email  = jwtUtil.extractEmail(token);
                Long   userId = jwtUtil.extractUserId(token);

                log.debug("JWT valid: email={}, userId={}", email, userId);

                var auth = new UsernamePasswordAuthenticationToken(
                        email, null, Collections.emptyList()
                );
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                log.warn("JWT present but invalid for path={}", path);
            }
        } else {
            log.debug("No JWT token for path={}", path);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
//```
//
//        ---
//
//        ## You are now at Phase 4 complete ✓
//
//        OAuth2 is fully working. The dashboard will load (empty until you sync Gmail). Here's your current status:
//        ```
//        ✓ Phase 1 — Database schema + project structure
//        ✓ Phase 2 — Google OAuth2 + JWT auth
//        ✓ Phase 3 — Gmail fetching + email parsing
//        ✓ Phase 4 — REST APIs + React frontend + Login working
//        → Phase 5 — @Scheduled reminders (next)