package com.buggybot.store.controller.store.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

/**
 * StoreDTO - Data Transfer Object for Store
 *
 * Note: userId is included in responses but NOT required in requests
 * The userId is automatically set from the authenticated user's JWT token
 */
public record StoreDTO(
    UUID storeId,

    @NotBlank(message = "storeName is required")
    String storeName,

    @NotBlank(message = "storeLocation is required")
    String storeLocation,

    Instant storeCreatedAt,

    // userId is returned in GET responses (who owns this store)
    // but NOT required in POST/PUT (automatically set from JWT)
    UUID userId
) {
    // Compact constructor for additional validation if needed
    public StoreDTO {
        // Validation is handled by Jakarta annotations
    }
}
