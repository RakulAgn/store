package com.buggybot.store.controller.store;

import com.buggybot.store.controller.common.PaginatedResponse;
import com.buggybot.store.controller.store.dto.StoreDTO;
import com.buggybot.store.controller.store.repository.StoreRepository;
import com.buggybot.store.controller.store.responseEntity.Store;
import com.buggybot.store.controller.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * StoreServiceImpl - Business logic for store operations
 *
 * Now includes user ownership:
 * - Stores are linked to users
 * - Users can only access their own stores
 */
@Service
public class StoreServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(StoreServiceImpl.class);
    private final StoreRepository storeRepository;

    public StoreServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    /**
     * Get paginated stores for a specific user
     * Users can only see their own stores
     */
    public PaginatedResponse<Store> getStoresPaginated(int page, int size, User user) {
        // Convert 1-based page to 0-based for Spring Data
        int zeroBasedPage = page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size);
        Page<Store> storePage = storeRepository.findByUser(user, pageable);

        logger.info("Fetched {} stores for user {}", storePage.getTotalElements(), user.getUserId());

        // Return response with 1-based page number
        return new PaginatedResponse<>(
                storePage.getContent(),
                page, // Return the original 1-based page number
                storePage.getSize(),
                storePage.getTotalElements(),
                storePage.getTotalPages(),
                storePage.isFirst(),
                storePage.isLast()
        );
    }

    public Optional<Store> getStoreById(UUID id) {
        if (id == null) return Optional.empty();
        return storeRepository.findById(id);
    }

    /**
     * Create a new store for a user
     * The store is automatically linked to the authenticated user
     */
    public Store createStore(StoreDTO dto, User user) {
        Store newStore = new Store();
        newStore.setStoreName(dto.storeName());
        newStore.setStoreLocation(dto.storeLocation());
        newStore.setUser(user);  // Link store to user

        // Only set ID if provided, otherwise let JPA generate it
        if (dto.storeId() != null) {
            newStore.setStoreId(dto.storeId());
        }

        // Only set createdAt if provided, otherwise let @CreationTimestamp handle it
        if (dto.storeCreatedAt() != null) {
            newStore.setStoreCreatedAt(dto.storeCreatedAt());
        }

        Store savedStore = storeRepository.save(newStore);
        logger.info("Created store {} for user {} (total={})",
            savedStore.getStoreId(), user.getUserId(), storeRepository.count());
        return savedStore;
    }

    /**
     * Replace a store (full update)
     * Includes ownership check - users can only update their own stores
     */
    public Optional<Store> replaceStore(UUID id, StoreDTO dto, User user) {
        if (id == null) return Optional.empty();

        return storeRepository.findById(id)
            .filter(store -> store.getUser().getUserId().equals(user.getUserId())) // Check ownership
            .map(existingStore -> {
                existingStore.setStoreName(dto.storeName());
                existingStore.setStoreLocation(dto.storeLocation());

                // Update createdAt if provided in DTO
                if (dto.storeCreatedAt() != null) {
                    existingStore.setStoreCreatedAt(dto.storeCreatedAt());
                }

                Store updatedStore = storeRepository.save(existingStore);
                logger.info("Replaced store {} for user {}", id, user.getUserId());
                return updatedStore;
            });
    }

    /**
     * Partially update a store (PATCH)
     * Includes ownership check - users can only update their own stores
     */
    public Optional<Store> patchStore(UUID id, Map<String, Object> updates, User user) {
        if (id == null) return Optional.empty();

        return storeRepository.findById(id)
            .filter(store -> store.getUser().getUserId().equals(user.getUserId())) // Check ownership
            .map(existingStore -> {
                // Update storeName if provided
                if (updates.containsKey("storeName")) {
                    existingStore.setStoreName(String.valueOf(updates.get("storeName")));
                }

                // Update storeLocation if provided
                if (updates.containsKey("storeLocation")) {
                    existingStore.setStoreLocation(String.valueOf(updates.get("storeLocation")));
                }

                // Update storeCreatedAt if provided
                if (updates.containsKey("storeCreatedAt") && updates.get("storeCreatedAt") != null) {
                    String raw = String.valueOf(updates.get("storeCreatedAt"));
                    existingStore.setStoreCreatedAt(Instant.parse(raw)); // may throw DateTimeParseException
                }

                // Handle ID change if requested
                if (updates.containsKey("storeId") && updates.get("storeId") != null) {
                    UUID newId = UUID.fromString(String.valueOf(updates.get("storeId")));
                    if (!Objects.equals(id, newId)) {
                        // Delete old entity and create new one with new ID
                        storeRepository.deleteById(id);
                        existingStore.setStoreId(newId);
                    }
                }

                Store updatedStore = storeRepository.save(existingStore);
                logger.info("Patched store {} for user {}", id, user.getUserId());
                return updatedStore;
            });
    }

    /**
     * Delete a store
     * Includes ownership check - users can only delete their own stores
     */
    public boolean deleteStore(UUID id, User user) {
        if (id == null) return false;

        Optional<Store> store = storeRepository.findById(id);

        // Check if store exists AND belongs to the user
        if (store.isPresent() && store.get().getUser().getUserId().equals(user.getUserId())) {
            storeRepository.deleteById(id);
            logger.info("Deleted store {} for user {}", id, user.getUserId());
            return true;
        }

        return false; // Store not found or doesn't belong to user
    }

    /**
     * Convert Store entity to DTO
     */
    public StoreDTO toDTO(Store store) {
        return new StoreDTO(
            store.getStoreId(),
            store.getStoreName(),
            store.getStoreLocation(),
            store.getStoreCreatedAt(),
            store.getUser().getUserId()
        );
    }
}
