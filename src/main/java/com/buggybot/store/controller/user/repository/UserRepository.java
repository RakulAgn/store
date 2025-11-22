package com.buggybot.store.controller.user.repository;

import com.buggybot.store.controller.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * UserRepository - Database access for User entity
 *
 * This interface extends JpaRepository which provides:
 * - findAll() - Get all users
 * - findById(UUID id) - Find user by internal ID
 * - save(User user) - Create or update user
 * - deleteById(UUID id) - Delete user
 * - count() - Count total users
 *
 * Custom Methods:
 * Spring Data JPA automatically implements these based on method names!
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their Auth0 ID
     * This is crucial for mapping Auth0 users to our database users
     *
     * Spring Data JPA automatically creates the query:
     * SELECT * FROM users WHERE auth0_id = ?
     */
    Optional<User> findByAuth0Id(String auth0Id);

    /**
     * Find a user by email
     * Useful for user lookup and duplicate checking
     *
     * Automatic query: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with this Auth0 ID
     *
     * Automatic query: SELECT COUNT(*) > 0 FROM users WHERE auth0_id = ?
     */
    boolean existsByAuth0Id(String auth0Id);
}
