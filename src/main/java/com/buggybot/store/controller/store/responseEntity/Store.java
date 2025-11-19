package com.buggybot.store.controller.store.responseEntity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "stores")
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

    // Default constructor for JPA
    public Store() {
    }

    // Constructor with all fields
    public Store(UUID storeId, String storeName, String storeLocation, Instant storeCreatedAt) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeLocation = storeLocation;
        this.storeCreatedAt = storeCreatedAt;
    }

    // Constructor without auto-generated fields
    public Store(String storeName, String storeLocation) {
        this.storeName = storeName;
        this.storeLocation = storeLocation;
    }

    // Getters and Setters
    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public Instant getStoreCreatedAt() {
        return storeCreatedAt;
    }

    public void setStoreCreatedAt(Instant storeCreatedAt) {
        this.storeCreatedAt = storeCreatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(storeId, store.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId);
    }

    @Override
    public String toString() {
        return "Store{" +
                "storeId=" + storeId +
                ", storeName='" + storeName + '\'' +
                ", storeLocation='" + storeLocation + '\'' +
                ", storeCreatedAt=" + storeCreatedAt +
                '}';
    }
}
