package com.buggybot.store.controller.store;


import com.buggybot.store.controller.common.ApiResponse;
import com.buggybot.store.controller.store.dto.StoreDTO;
import com.buggybot.store.controller.store.responseEntity.Store;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController()
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);
    private final List<Store> stores = new CopyOnWriteArrayList<>();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Store>>> getStores() {
        try {
            logger.info("Fetching all stores. Total: {}", stores.size());
            ApiResponse<List<Store>> resp = new ApiResponse<>(true, null, stores);
            return ResponseEntity.ok(resp);
        } catch (Exception error) {
            logger.error("Error fetching stores", error);
            ApiResponse<List<Store>> resp = new ApiResponse<>(false, "Failed to Get Stores",null );
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    @PostMapping("/new/store")
    public ResponseEntity<ApiResponse<Store>> createNewStore(@Valid @RequestBody StoreDTO storeData) {
        try {
            logger.info("Store created: {}", storeData);

            Store createdStore = new Store(
                    storeData.getStoreName(),
                    storeData.getStoreLocation(),
                    storeData.getStoreCreatedAt() != null ? storeData.getStoreCreatedAt() : Instant.now(),
                    storeData.getStoreId() != null ? storeData.getStoreId() : UUID.randomUUID()
            );

            stores.add(createdStore);

            ApiResponse<Store> resp = new ApiResponse<>(
                    true,
                    "Store Created Successfully", // no message this time
                    null
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            logger.error("Error creating store", e);
            ApiResponse<Store> resp = new ApiResponse<>(
                    false,
                    "Failed to Create Store",
                    null
            );
            return ResponseEntity.internalServerError().body(resp);
        }
    }
}
