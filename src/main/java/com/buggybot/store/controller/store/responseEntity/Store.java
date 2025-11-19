package com.buggybot.store.controller.store.responseEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

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

    // Constructor without auto-generated fields
    public Store(String storeName, String storeLocation) {
        this.storeName = storeName;
        this.storeLocation = storeLocation;
    }
}
