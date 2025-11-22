package com.buggybot.store.controller.user;

import com.buggybot.store.controller.common.ApiResponse;
import com.buggybot.store.controller.user.dto.UserDTO;
import com.buggybot.store.controller.user.entity.User;
import com.buggybot.store.controller.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UserController - API endpoints for user operations
 *
 * Endpoints:
 * - GET /api/user/me - Get current user profile
 * - GET /api/user/all - Get all users (admin)
 * - PATCH /api/user/me - Update current user profile
 * - DELETE /api/user/me - Delete current user account
 *
 * Note: All endpoints require authentication (JWT token)
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/user/me - Get current authenticated user
     *
     * This endpoint:
     * 1. Extracts user info from JWT token
     * 2. Syncs user to database if not exists
     * 3. Returns user profile
     *
     * Authentication object is automatically injected by Spring Security
     * when a valid JWT token is present in the request header
     *
     * @param authentication Automatically injected by Spring Security
     * @return Current user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication authentication) {
        try {
            logger.info("Getting current user profile");

            // Get or create user in database (auto-sync from Auth0)
            User user = userService.getOrCreateUser(authentication);

            // Convert to DTO and return
            UserDTO userDTO = userService.toDTO(user);
            return ResponseEntity.ok(new ApiResponse<>(true, null, userDTO));

        } catch (Exception e) {
            logger.error("Error getting current user", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, "Failed to get user profile", null));
        }
    }

    /**
     * GET /api/user/all - Get all users
     *
     * This is an admin endpoint to see all users in the system
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(Authentication authentication) {
        try {
            logger.info("Getting all users");

            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOs = users.stream()
                .map(userService::toDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, null, userDTOs));

        } catch (Exception e) {
            logger.error("Error getting all users", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, "Failed to get users", null));
        }
    }

    /**
     * PATCH /api/user/me - Update current user profile
     *
     * Allows users to update their name and email
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUser(
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        try {
            User user = userService.getOrCreateUser(authentication);
            logger.info("Updating user {} with: {}", user.getUserId(), updates);

            // Update name if provided
            if (updates.containsKey("name")) {
                user.setName(String.valueOf(updates.get("name")));
            }

            // Update email if provided
            if (updates.containsKey("email")) {
                user.setEmail(String.valueOf(updates.get("email")));
            }

            User updatedUser = userService.updateUser(user);
            UserDTO userDTO = userService.toDTO(updatedUser);

            return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", userDTO));

        } catch (Exception e) {
            logger.error("Error updating user", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, "Failed to update user", null));
        }
    }

    /**
     * DELETE /api/user/me - Delete current user account
     *
     * This deletes the user from our database (NOT from Auth0)
     * WARNING: This will also delete all stores owned by this user (cascade)
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUser(Authentication authentication) {
        try {
            User user = userService.getOrCreateUser(authentication);
            logger.info("Deleting user {}", user.getUserId());

            userService.deleteUser(user.getUserId());

            return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));

        } catch (Exception e) {
            logger.error("Error deleting user", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, "Failed to delete user", null));
        }
    }
}
