package com.buggybot.store.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * AudienceValidator - Validates the 'aud' claim in JWT tokens
 *
 * What is Audience?
 * - The 'aud' (audience) claim in a JWT identifies WHO the token is for
 * - It's like checking if a concert ticket is for the right venue
 * - Without this check, tokens meant for OTHER APIs could access YOUR API
 *
 * Why do we need this?
 * - Auth0 can issue tokens for multiple APIs
 * - We need to ensure the token is specifically for OUR API
 * - Security best practice to prevent token misuse
 *
 * How it works:
 * - Extracts the 'aud' claim from the JWT
 * - Compares it with our configured audience
 * - Rejects the token if it doesn't match
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    /**
     * Validates the JWT token's audience claim
     *
     * @param jwt The JWT token to validate
     * @return ValidationResult - success or failure with error
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // Get the 'aud' claim from the token
        // JWT tokens can have multiple audiences (list)
        if (jwt.getAudience().contains(audience)) {
            // Token is meant for our API - valid!
            return OAuth2TokenValidatorResult.success();
        }

        // Token is NOT for our API - reject it!
        OAuth2Error error = new OAuth2Error(
            "invalid_token",
            "The required audience is missing",
            null
        );
        return OAuth2TokenValidatorResult.failure(error);
    }
}
