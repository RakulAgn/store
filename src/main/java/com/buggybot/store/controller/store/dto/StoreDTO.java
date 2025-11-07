package com.buggybot.store.controller.store.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class StoreDTO {


    private UUID storeId;

    @NotBlank(message = "storeName is required")
    private String storeName;

    @NotBlank(message = "storeLocation is required")
    private String storeLocation;

    private Instant storeCreatedAt;

    // Getters and setters
    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreLocation() { return storeLocation; }
    public void setStoreLocation(String storeLocation) { this.storeLocation = storeLocation; }

    public Instant getStoreCreatedAt() { return storeCreatedAt; }
    public void setStoreCreatedAt(Instant storeCreatedAt) { this.storeCreatedAt = storeCreatedAt; }
}
