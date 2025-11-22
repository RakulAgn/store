package com.buggybot.store.controller.store.responseEntity;

import com.buggybot.store.controller.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Store Entity - Represents a store in the database
 *
 * Relationships:
 * - Many stores belong to one user (ManyToOne)
 */
@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id", updatable = false, nullable = false)
    private UUID storeId;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "store_location", nullable = false)
    private String storeLocation;

    @CreationTimestamp
    @Column(name = "store_created_at", updatable = false, nullable = false)
    private Instant storeCreatedAt;

    /**
     * Relationship: Many stores belong to one user
     *
     * @ManyToOne - This store belongs to ONE user
     * @JoinColumn - Creates a foreign key column "user_id" in stores table
     * FetchType.LAZY - Don't load user automatically (saves memory)
     *
     * Database: stores table will have a "user_id" column referencing users table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructor without auto-generated fields
    public Store(String storeName, String storeLocation, User user) {
        this.storeName = storeName;
        this.storeLocation = storeLocation;
        this.user = user;
    }
}
