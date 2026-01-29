package uz.garantbank.okrTrackingSystem.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.garantbank.okrTrackingSystem.dto.UserDTO;
import uz.garantbank.okrTrackingSystem.dto.UserWithScoreDTO;
import uz.garantbank.okrTrackingSystem.dto.user.AssignDepartmentsRequest;
import uz.garantbank.okrTrackingSystem.dto.user.CreateUserRequest;
import uz.garantbank.okrTrackingSystem.dto.user.UpdateUserRequest;
import uz.garantbank.okrTrackingSystem.dto.user.UserProfileDTO;
import uz.garantbank.okrTrackingSystem.entity.Role;
import uz.garantbank.okrTrackingSystem.entity.User;
import uz.garantbank.okrTrackingSystem.service.DepartmentAccessService;
import uz.garantbank.okrTrackingSystem.service.UserService;

import java.util.List;
import java.util.UUID;

/**
 * Controller for user management operations.
 * Most operations require ADMIN role, some allow self-modification.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class UserManagementController {


    private final UserService userService;
    private final DepartmentAccessService accessService;

    /**
     * Get all users (ADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get all users with their overall scores (ADMIN and DIRECTOR)
     * Used for the Team Overview page
     */
    @GetMapping("/with-scores")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<UserWithScoreDTO>> getAllUsersWithScores() {
        return ResponseEntity.ok(userService.getAllUsersWithScores());
    }

    /**
     * Get user by ID (ADMIN or self)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        User currentUser = accessService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isDirector = currentUser.getRole() == Role.DIRECTOR;
        boolean isSelf = currentUser.getId().equals(id);

        if (!isAdmin && !isDirector && !isSelf) {
            throw new AccessDeniedException("You can only view your own profile");
        }

        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Create a new user (ADMIN only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    /**
     * Update user (ADMIN for all fields, self for profile fields only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        User currentUser = accessService.getCurrentUser();
        return ResponseEntity.ok(userService.updateUser(id, request, currentUser));
    }

    /**
     * Delete user (ADMIN only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        User currentUser = accessService.getCurrentUser();

        // Prevent self-deletion
        if (currentUser.getId().equals(id)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign departments to a user (ADMIN only)
     */
    @PostMapping("/{id}/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> assignDepartments(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDepartmentsRequest request) {
        return ResponseEntity.ok(userService.assignDepartments(id, request.getDepartmentIds()));
    }

    /**
     * Remove a department from a user (ADMIN only)
     */
    @DeleteMapping("/{id}/departments/{deptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> removeDepartment(
            @PathVariable UUID id,
            @PathVariable String deptId) {
        return ResponseEntity.ok(userService.removeDepartment(id, deptId));
    }

    /**
     * Upload profile photo (ADMIN or self)
     */
    @PostMapping("/{id}/photo")
    public ResponseEntity<UserDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestParam("photo") MultipartFile file) {
        User currentUser = accessService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isSelf = currentUser.getId().equals(id);

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("You can only upload your own profile photo");
        }

        return ResponseEntity.ok(userService.uploadPhoto(id, file));
    }

    /**
     * Get users by department
     */
    @GetMapping("/by-department/{deptId}")
    public ResponseEntity<List<UserDTO>> getUsersByDepartment(@PathVariable String deptId) {
        return ResponseEntity.ok(userService.getUsersByDepartment(deptId));
    }

    /**
     * Get current user's extended profile
     */
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileDTO> getMyProfile() {
        User currentUser = accessService.getCurrentUser();
        return ResponseEntity.ok(userService.getProfile(currentUser.getId()));
    }



}
