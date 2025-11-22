package com.buggybot.store.controller.user.entity;

import com.buggybot.store.controller.store.responseEntity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User Entity - Represents a user in the database
 *
 * This entity stores user information synchronized from Auth0.
 * Each user in our system corresponds to a user in Auth0.
 *
 * Fields explained:
 * - userId: Our internal UUID (Primary Key)
 * - auth0Id: The unique identifier from Auth0 (like "auth0|123456")
 * - email: User's email address
 * - name: User's display name
 * - createdAt: When the user was first created in our DB
 * - updatedAt: When the user info was last updated
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    /**
     * auth0Id: The unique identifier from Auth0
     * Format: "auth0|1234567890" or "google-oauth2|123456"
     * This is UNIQUE - each Auth0 user maps to one database user
     */
    @Column(name = "auth0_id", unique = true, nullable = false)
    private String auth0Id;

    // Email can be null for machine-to-machine tokens
    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * One user can have many stores
     * CASCADE.ALL: When user is deleted, all their stores are deleted too
     * orphanRemoval: If a store is removed from this list, delete it from DB
     * mappedBy: The "user" field in Store entity owns this relationship
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Store> stores = new ArrayList<>();

    // Constructor without auto-generated fields (for creating new users)
    public User(String auth0Id, String email, String name) {
        this.auth0Id = auth0Id;
        this.email = email;
        this.name = name;
    }
}
