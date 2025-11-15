package com.buggybot.store.controller.store;

import com.buggybot.store.controller.common.ApiResponse;
import com.buggybot.store.controller.store.dto.StoreDTO;
import com.buggybot.store.controller.store.interfaces.StoreService;
import com.buggybot.store.controller.store.responseEntity.Store;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

    @Autowired()
    private StoreService storeService;

//    // Constructor injection (preferred for testing and immutability)
//    public StoreController(StoreService storeService) {
//        this.storeService = storeService;
//    }

    // GET /api/store/all
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Store>>> getStores() {
        try {
            List<Store> stores = storeService.getAllStores();
            logger.info("Fetching all stores. Total: {}", stores.size());
            return ResponseEntity.ok(new ApiResponse<>(true, null, stores));
        } catch (Exception e) {
            logger.error("Error fetching stores", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to Get Stores", null));
        }
    }

    // GET /api/store/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> getStore(@PathVariable UUID id) {
        try {
            logger.info("Fetching store for id: {}", id);
            Optional<Store> maybe = storeService.getStoreById(id);
            if (maybe.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Store not found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, null, maybe.get()));
        } catch (Exception e) {
            logger.error("Error fetching store with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to get store", null));
        }
    }

    // POST /api/store
    @PostMapping
    public ResponseEntity<ApiResponse<Store>> createNewStore(@Valid @RequestBody StoreDTO storeData) {
        try {
            logger.info("Creating new store: {}", storeData);
            Store created = storeService.createStore(storeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Store Created Successfully", created));
        } catch (Exception e) {
            logger.error("Error creating store", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to Create Store", null));
        }
    }

    // PUT /api/store/{id}  - full replace
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> replaceStore(
            @PathVariable UUID id,
            @Valid @RequestBody StoreDTO storeData) {
        try {
            logger.info("Replacing store with id: {}", id);
            Optional<Store> replaced = storeService.replaceStore(id, storeData);
            if (replaced.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Store not found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Store replaced successfully", replaced.get()));
        } catch (Exception e) {
            logger.error("Error replacing store with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to replace store", null));
        }
    }

    // PATCH /api/store/{id} - partial update
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> patchStore(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updates) {
        try {
            logger.info("Patching store with id: {} updates: {}", id, updates);
            Optional<Store> updated = storeService.patchStore(id, updates);
            if (updated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Store not found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Store updated", updated.get()));
        } catch (java.time.format.DateTimeParseException dtpe) {
            logger.warn("Invalid date format in patch for id: {}", id, dtpe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Invalid date format for storeCreatedAt. Use ISO-8601.", null));
        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid value in patch for id: {}", id, iae);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Invalid value in payload: " + iae.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error patching store with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to patch store", null));
        }
    }

    // DELETE /api/store/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable UUID id) {
        try {
            logger.info("Deleting store with id: {}", id);
            boolean removed = storeService.deleteStore(id);
            if (!removed) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Store not found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Store deleted successfully", null));
        } catch (Exception e) {
            logger.error("Error deleting store with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to delete store", null));
        }
    }
}
