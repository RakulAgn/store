package com.buggybot.store.controller.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

/**
 * UserDTO - Data Transfer Object for User
 *
 * This is used to transfer user data between layers of the application.
 * DTOs help separate internal database representation from API responses.
 *
 * Why use DTOs?
 * 1. Security - Don't expose internal database fields
 * 2. Flexibility - API can change without changing database
 * 3. Validation - Ensure data is correct before processing
 */
public record UserDTO(
    UUID userId,

    String auth0Id,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    String name,

    Instant createdAt,

    Instant updatedAt
) {
    // Records automatically generate:
    // - Constructor with all fields
    // - Getters for all fields
    // - equals(), hashCode(), toString()
}
