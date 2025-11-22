package com.buggybot.store.controller.user.service;

import com.buggybot.store.controller.user.dto.UserDTO;
import com.buggybot.store.controller.user.entity.User;
import com.buggybot.store.controller.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserService - Manages user operations and Auth0 synchronization
 *
 * This service handles:
 * 1. Syncing Auth0 users with our database
 * 2. Retrieving user information
 * 3. Extracting user from JWT tokens
 *
 * Why sync users to database?
 * - Link users to their data (stores, orders, etc.)
 * - Add custom fields not in Auth0
 * - Faster queries (no need to call Auth0 API)
 * - Works even if Auth0 is temporarily down
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get or create user from Authentication object
     *
     * This is called when a user makes a request with a valid JWT token.
     * If the user doesn't exist in our DB, we create them.
     *
     * @param authentication Spring Security authentication object
     * @return User entity from database
     */
    @Transactional
    public User getOrCreateUser(Authentication authentication) {
        // Extract JWT from authentication
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // Get user info from JWT claims
        String auth0Id = jwt.getSubject();  // "auth0|123456" or "google-oauth2|123456"
        String email = jwt.getClaim("email");
        String name = jwt.getClaim("name");

        logger.info("Getting or creating user - auth0Id: {}, email: {}", auth0Id, email);

        // Check if user exists in database
        Optional<User> existingUser = userRepository.findByAuth0Id(auth0Id);

        if (existingUser.isPresent()) {
            // User exists - return it
            logger.debug("User found in database: {}", existingUser.get().getUserId());
            return existingUser.get();
        }

        // User doesn't exist - create new user
        User newUser = new User(auth0Id, email, name);
        User savedUser = userRepository.save(newUser);
        logger.info("New user created in database: {} (auth0Id: {})", savedUser.getUserId(), auth0Id);

        return savedUser;
    }

    /**
     * Get current authenticated user's Auth0 ID from JWT
     *
     * @param authentication Spring Security authentication
     * @return Auth0 ID (subject claim from JWT)
     */
    public String getCurrentUserAuth0Id(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }

    /**
     * Get current authenticated user from database
     *
     * @param authentication Spring Security authentication
     * @return User entity if found
     */
    public Optional<User> getCurrentUser(Authentication authentication) {
        String auth0Id = getCurrentUserAuth0Id(authentication);
        return userRepository.findByAuth0Id(auth0Id);
    }

    /**
     * Get user by internal UUID
     */
    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Get user by Auth0 ID
     */
    public Optional<User> getUserByAuth0Id(String auth0Id) {
        return userRepository.findByAuth0Id(auth0Id);
    }

    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users (admin function)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Update user information
     */
    @Transactional
    public User updateUser(User user) {
        logger.info("Updating user: {}", user.getUserId());
        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     * WARNING: This will cascade delete all stores owned by this user
     */
    @Transactional
    public void deleteUser(UUID userId) {
        logger.info("Deleting user: {}", userId);
        userRepository.deleteById(userId);
    }

    /**
     * Convert User entity to DTO
     */
    public UserDTO toDTO(User user) {
        return new UserDTO(
            user.getUserId(),
            user.getAuth0Id(),
            user.getEmail(),
            user.getName(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
