package com.buggybot.store.controller.store.responseEntity;

import java.time.Instant;
import java.util.UUID;

public record Store(
        String storeName,
        String storeLocation,
        Instant storeCreatedAt,
        UUID storeId
) {}
