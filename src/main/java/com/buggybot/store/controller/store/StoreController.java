package com.buggybot.store.controller.store;

import com.buggybot.store.controller.common.ApiResponse;
import com.buggybot.store.controller.common.PaginatedResponse;
import com.buggybot.store.controller.store.dto.StoreDTO;
import com.buggybot.store.controller.store.responseEntity.Store;
import com.buggybot.store.controller.user.entity.User;
import com.buggybot.store.controller.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * StoreController - REST API for Store operations
 *
 * Now with Authentication:
 * - All endpoints require a valid JWT token
 * - Users can only access their own stores
 * - Authentication object is auto-injected by Spring Security
 */
@RestController
@RequestMapping("/api/store")
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);
    private final StoreServiceImpl storeService;
    private final UserService userService;

    public StoreController(StoreServiceImpl storeService, UserService userService) {
        this.storeService = storeService;
        this.userService = userService;
    }

    /**
     * GET /api/store/all - Get all stores for current user (paginated)
     *
     * Authentication parameter is automatically injected by Spring Security
     * when a valid JWT token is in the Authorization header
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PaginatedResponse<Store>>> getStores(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {  // Auto-injected by Spring Security
        try {
            if (page < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Page number must be >= 1", null));
            }
            if (size < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Page size must be >= 1", null));
            }

            // Get or create user from JWT token (auto-sync from Auth0)
            User user = userService.getOrCreateUser(authentication);

            // Get only the authenticated user's stores
            PaginatedResponse<Store> paginatedStores = storeService.getStoresPaginated(page, size, user);
            logger.info("Fetching stores for user {} - page: {}, size: {}, total elements: {}",
                    user.getUserId(), page, size, paginatedStores.totalElements());
            return ResponseEntity.ok(new ApiResponse<>(true, null, paginatedStores));
        } catch (Exception e) {
            logger.error("Error fetching stores", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to Get Stores", null));
        }
    }

    /**
     * GET /api/store/{id} - Get a specific store
     *
     * Only allows accessing stores owned by the authenticated user
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> getStore(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            User user = userService.getOrCreateUser(authentication);

            logger.info("Fetching store {} for user {}", id, user.getUserId());
            Optional<Store> maybe = storeService.getStoreById(id);

            // Check if store exists AND belongs to the user
            if (maybe.isEmpty() || !maybe.get().getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Store not found or access denied", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, null, maybe.get()));
        } catch (Exception e) {
            logger.error("Error fetching store with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to get store", null));
        }
    }

    /**
     * POST /api/store - Create a new store for the authenticated user
     *
     * The store is automatically linked to the user from the JWT token
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Store>> createNewStore(
            @Valid @RequestBody StoreDTO storeData,
            Authentication authentication) {
        try {
            // Get or create user from JWT token
            User user = userService.getOrCreateUser(authentication);

            logger.info("Creating new store for user {}: {}", user.getUserId(), storeData);
            Store created = storeService.createStore(storeData, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Store Created Successfully", created));
        } catch (Exception e) {
            logger.error("Error creating store", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to Create Store", null));
        }
    }

    /**
     * PUT /api/store/{id} - Replace a store (full update)
     *
     * Only allows updating stores owned by the authenticated user
     * Returns 404 if store doesn't exist OR doesn't belong to user
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> replaceStore(
            @PathVariable UUID id,
            @Valid @RequestBody StoreDTO storeData,
            Authentication authentication) {
        try {
            User user = userService.getOrCreateUser(authentication);

            logger.info("Replacing store {} for user {}", id, user.getUserId());
            Optional<Store> replaced = storeService.replaceStore(id, storeData, user);
            if (replaced.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Store not found or access denied", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Store replaced successfully", replaced.get()));
        } catch (Exception e) {
            logger.error("Error replacing store with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to replace store", null));
        }
    }

    /**
     * PATCH /api/store/{id} - Partially update a store
     *
     * Only allows updating stores owned by the authenticated user
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> patchStore(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        try {
            User user = userService.getOrCreateUser(authentication);

            logger.info("Patching store {} for user {} with updates: {}", id, user.getUserId(), updates);
            Optional<Store> updated = storeService.patchStore(id, updates, user);
            if (updated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Store not found or access denied", null));
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

    /**
     * DELETE /api/store/{id} - Delete a store
     *
     * Only allows deleting stores owned by the authenticated user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            User user = userService.getOrCreateUser(authentication);

            logger.info("Deleting store {} for user {}", id, user.getUserId());
            boolean removed = storeService.deleteStore(id, user);
            if (!removed) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Store not found or access denied", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Store deleted successfully", null));
        } catch (Exception e) {
            logger.error("Error deleting store with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to delete store", null));
        }
    }
}
