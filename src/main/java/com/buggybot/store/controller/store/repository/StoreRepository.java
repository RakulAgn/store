package com.buggybot.store.controller.store.repository;

import com.buggybot.store.controller.store.responseEntity.Store;
import com.buggybot.store.controller.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * StoreRepository - Database operations for Store entity
 *
 * Custom queries for user-specific store access
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    // JpaRepository provides the following methods automatically:
    // - findAll() -> List<Store>
    // - findById(UUID id) -> Optional<Store>
    // - save(Store entity) -> Store
    // - deleteById(UUID id) -> void
    // - existsById(UUID id) -> boolean
    // - count() -> long

    /**
     * Find all stores belonging to a specific user
     * Spring Data JPA auto-generates: SELECT * FROM stores WHERE user_id = ?
     */
    List<Store> findByUser(User user);

    /**
     * Find stores by user with pagination
     * Essential for listing a user's stores with page support
     */
    Page<Store> findByUser(User user, Pageable pageable);

    /**
     * Count stores owned by a user
     */
    long countByUser(User user);
}
