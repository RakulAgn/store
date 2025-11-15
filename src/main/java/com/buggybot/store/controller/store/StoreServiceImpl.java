package com.buggybot.store.controller.store;

import com.buggybot.store.controller.store.dto.StoreDTO;
import com.buggybot.store.controller.store.interfaces.StoreService;
import com.buggybot.store.controller.store.responseEntity.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StoreServiceImpl implements StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreServiceImpl.class);
    private final CopyOnWriteArrayList<Store> stores = new CopyOnWriteArrayList<>();

    @Override
    public List<Store> getAllStores() {
        return new ArrayList<>(stores);
    }

    @Override
    public Optional<Store> getStoreById(UUID id) {
        if (id == null) return Optional.empty();

        return stores.stream()
                .filter(s -> {
                    UUID sid = s.storeId();
                    return sid != null && sid.equals(id);
                })
                .findFirst();
    }

    @Override
    public Store createStore(StoreDTO dto) {
        UUID id = dto.getStoreId() != null ? dto.getStoreId() : UUID.randomUUID();
        Instant createdAt = dto.getStoreCreatedAt() != null ? dto.getStoreCreatedAt() : Instant.now();

        Store created = new Store(dto.getStoreName(), dto.getStoreLocation(), createdAt, id);

        stores.add(created); // safe with CopyOnWriteArrayList
        logger.info("Created store {} (total={})", id, stores.size());
        return created;
    }

    @Override
    public Optional<Store> replaceStore(UUID id, StoreDTO dto) {
        if (id == null) return Optional.empty();

        // find index
        int idx = indexOfStore(id);
        if (idx == -1) return Optional.empty();

        Instant createdAt = dto.getStoreCreatedAt() != null ? dto.getStoreCreatedAt() : Instant.now();

        Store replaced = new Store(dto.getStoreName(), dto.getStoreLocation(), createdAt, id);

        // set is supported by CopyOnWriteArrayList
        stores.set(idx, replaced);

        return Optional.of(replaced);
    }

    @Override
    public Optional<Store> patchStore(UUID id, Map<String, Object> updates) {
        if (id == null) return Optional.empty();

        int idx = indexOfStore(id);
        if (idx == -1) return Optional.empty();

        Store existing = stores.get(idx);

        String newName = updates.containsKey("storeName")
                ? String.valueOf(updates.get("storeName"))
                : existing.storeName();

        String newLocation = updates.containsKey("storeLocation")
                ? String.valueOf(updates.get("storeLocation"))
                : existing.storeLocation();

        Instant newCreatedAt = existing.storeCreatedAt();
        if (updates.containsKey("storeCreatedAt") && updates.get("storeCreatedAt") != null) {
            String raw = String.valueOf(updates.get("storeCreatedAt"));
            newCreatedAt = Instant.parse(raw); // may throw DateTimeParseException
        }

        UUID newId = existing.storeId();
        if (updates.containsKey("storeId") && updates.get("storeId") != null) {
            newId = UUID.fromString(String.valueOf(updates.get("storeId")));
        }

        Store updated = new Store(newName, newLocation, newCreatedAt, newId);

        // If id changed, remove old and add new; else set in same index
        if (!Objects.equals(id, newId)) {
            // remove old instance (removeIf is atomic-ish and OK for COWAL)
            stores.removeIf(s -> {
                UUID sid = s.storeId();
                return sid != null && sid.equals(id);
            });
            stores.add(updated);
        } else {
            stores.set(idx, updated);
        }

        return Optional.of(updated);
    }

    @Override
    public boolean deleteStore(UUID id) {
        if (id == null) return false;
        return stores.removeIf(store -> {
            UUID sid = store.storeId();
            return sid != null && sid.equals(id);
        });
    }

    private int indexOfStore(UUID id) {
        for (int i = 0; i < stores.size(); i++) {
            Store s = stores.get(i);
            UUID sid = s.storeId();
            if (sid != null && sid.equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
