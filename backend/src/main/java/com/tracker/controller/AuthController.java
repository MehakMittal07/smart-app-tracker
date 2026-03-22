package com.tracker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Catch any stray redirect to /login on the backend
    // and send the browser to React instead
    @GetMapping("/login")
    public String loginRedirect() {
        return "redirect:" + frontendUrl + "/login";
    }

    // Same for /login?error
    @GetMapping(value = "/login", params = "error")
    public String loginError() {
        return "redirect:" + frontendUrl + "/login?error=oauth_failed";
    }
}