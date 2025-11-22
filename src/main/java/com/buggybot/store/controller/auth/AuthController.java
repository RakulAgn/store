package com.buggybot.store.controller.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AuthController - Public endpoints for authentication information
 *
 * This controller provides information about how to signup and login.
 * Actual authentication is handled by Auth0.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * GET /api/auth/info - Get authentication instructions
     * This is a public endpoint (no token required)
     */
    @GetMapping("/info")
    public Map<String, Object> getAuthInfo() {
        return Map.of(
            "message", "Authentication is handled by Auth0",
            "auth0_domain", "dev-ckpiff0e.us.auth0.com",
            "audience", "https://buggybot-api.com",
            "instructions", Map.of(
                "signup", "Create a user account in Auth0 Dashboard or use the signup endpoint",
                "login", "Use Auth0's token endpoint to get an access token",
                "token_url", "https://dev-ckpiff0e.us.auth0.com/oauth/token"
            ),
            "example_login_request", Map.of(
                "url", "POST https://dev-ckpiff0e.us.auth0.com/oauth/token",
                "body", Map.of(
                    "grant_type", "password",
                    "username", "user@example.com",
                    "password", "YourPassword123!",
                    "audience", "https://buggybot-api.com",
                    "client_id", "YOUR_CLIENT_ID",
                    "client_secret", "YOUR_CLIENT_SECRET"
                )
            )
        );
    }
}
