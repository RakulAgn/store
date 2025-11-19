package com.buggybot.store.controller.store;

import com.buggybot.store.controller.store.dto.StoreDTO;
import com.buggybot.store.controller.store.interfaces.StoreService;
import com.buggybot.store.controller.store.repository.StoreRepository;
import com.buggybot.store.controller.store.responseEntity.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class StoreServiceImpl implements StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreServiceImpl.class);
    private final StoreRepository storeRepository;

    public StoreServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    @Override
    public Optional<Store> getStoreById(UUID id) {
        if (id == null) return Optional.empty();
        return storeRepository.findById(id);
    }

    @Override
    public Store createStore(StoreDTO dto) {
        Store newStore = new Store();
        newStore.setStoreName(dto.getStoreName());
        newStore.setStoreLocation(dto.getStoreLocation());

        // Only set ID if provided, otherwise let JPA generate it
        if (dto.getStoreId() != null) {
            newStore.setStoreId(dto.getStoreId());
        }

        // Only set createdAt if provided, otherwise let @CreationTimestamp handle it
        if (dto.getStoreCreatedAt() != null) {
            newStore.setStoreCreatedAt(dto.getStoreCreatedAt());
        }

        Store savedStore = storeRepository.save(newStore);
        logger.info("Created store {} (total={})", savedStore.getStoreId(), storeRepository.count());
        return savedStore;
    }

    @Override
    public Optional<Store> replaceStore(UUID id, StoreDTO dto) {
        if (id == null) return Optional.empty();

        return storeRepository.findById(id).map(existingStore -> {
            existingStore.setStoreName(dto.getStoreName());
            existingStore.setStoreLocation(dto.getStoreLocation());

            // Update createdAt if provided in DTO
            if (dto.getStoreCreatedAt() != null) {
                existingStore.setStoreCreatedAt(dto.getStoreCreatedAt());
            }

            Store updatedStore = storeRepository.save(existingStore);
            logger.info("Replaced store {}", id);
            return updatedStore;
        });
    }

    @Override
    public Optional<Store> patchStore(UUID id, Map<String, Object> updates) {
        if (id == null) return Optional.empty();

        return storeRepository.findById(id).map(existingStore -> {
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
            logger.info("Patched store {}", id);
            return updatedStore;
        });
    }

    @Override
    public boolean deleteStore(UUID id) {
        if (id == null) return false;

        if (storeRepository.existsById(id)) {
            storeRepository.deleteById(id);
            logger.info("Deleted store {}", id);
            return true;
        }

        return false;
    }
}
