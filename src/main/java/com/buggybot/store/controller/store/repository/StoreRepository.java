package com.buggybot.store.controller.store.repository;

import com.buggybot.store.controller.store.responseEntity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    // JpaRepository provides the following methods automatically:
    // - findAll() -> List<Store>
    // - findById(UUID id) -> Optional<Store>
    // - save(Store entity) -> Store
    // - deleteById(UUID id) -> void
    // - existsById(UUID id) -> boolean
    // - count() -> long

    // You can add custom query methods here if needed
    // Examples:
    // List<Store> findByStoreName(String storeName);
    // List<Store> findByStoreLocation(String storeLocation);
}
