package com.buggybot.store.controller.store.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public record StoreDTO(
    UUID storeId,

    @NotBlank(message = "storeName is required")
    String storeName,

    @NotBlank(message = "storeLocation is required")
    String storeLocation,

    Instant storeCreatedAt
) {
    // Compact constructor for additional validation if needed
    public StoreDTO {
        // Validation is handled by Jakarta annotations
    }
}
