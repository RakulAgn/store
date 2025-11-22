package com.buggybot.store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig - Main Security Configuration
 *
 * This class configures Spring Security to work with Auth0.
 * It tells Spring Security how to validate JWT tokens and which endpoints to protect.
 *
 * Key Concepts:
 * - SecurityFilterChain: Defines security rules for HTTP requests
 * - JwtDecoder: Decodes and validates JWT tokens from Auth0
 * - OAuth2TokenValidator: Custom validation for audience claim
 */
@Configuration
@EnableWebSecurity  // Enables Spring Security
public class SecurityConfig {

    @Value("${auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    /**
     * SecurityFilterChain - Configures which endpoints are protected
     *
     * This is like a security guard at your API entrance.
     * It checks every request and decides:
     * 1. Is this endpoint public or protected?
     * 2. Does the user have a valid token?
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configure authorization rules
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints - anyone can access (no token needed)
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()  // Auth endpoints are public

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            // Configure OAuth2 Resource Server (JWT validation)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())  // Use our custom JWT decoder
                )
            )
            // Disable CSRF for stateless APIs (we use tokens, not sessions)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * JwtDecoder - Decodes and validates JWT tokens
     *
     * This bean does the heavy lifting:
     * 1. Fetches Auth0's public keys (to verify token signature)
     * 2. Validates the token hasn't been tampered with
     * 3. Checks issuer (who created the token)
     * 4. Checks audience (is this token meant for our API?)
     * 5. Checks expiration (is the token still valid?)
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Create default decoder from Auth0 issuer
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);

        // Create custom validators
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        // Combine all validators
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(
            withIssuer,
            audienceValidator
        );

        // Set the validators on the decoder
        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }
}
