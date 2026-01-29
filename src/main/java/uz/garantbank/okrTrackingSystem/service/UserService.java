package uz.garantbank.okrTrackingSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.garantbank.okrTrackingSystem.dto.*;
import uz.garantbank.okrTrackingSystem.dto.user.CreateUserRequest;
import uz.garantbank.okrTrackingSystem.dto.user.DepartmentSummaryDTO;
import uz.garantbank.okrTrackingSystem.dto.user.UpdateUserRequest;
import uz.garantbank.okrTrackingSystem.dto.user.UserProfileDTO;
import uz.garantbank.okrTrackingSystem.entity.Department;
import uz.garantbank.okrTrackingSystem.entity.Role;
import uz.garantbank.okrTrackingSystem.entity.User;
import uz.garantbank.okrTrackingSystem.repository.DepartmentRepository;
import uz.garantbank.okrTrackingSystem.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing users
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;
    private final ScoreCalculationService scoreCalculationService;

    /**
     * Register a new user (legacy method for backward compatibility)
     */
    @Transactional
    public UserDTO registerUser(RegisterRequest request) {
        CreateUserRequest createRequest = CreateUserRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .role(request.getRole())
                .assignedDepartmentIds(request.getDepartmentId() != null ?
                        Collections.singletonList(request.getDepartmentId()) : null)
                .build();
        return createUser(createRequest);
    }

    /**
     * Create a new user with full control over all fields
     */
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // Validate username and email are unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .jobTitle(request.getJobTitle())
                .phoneNumber(request.getPhoneNumber())
                .bio(request.getBio())
                .isActive(true)
                .assignedDepartments(new HashSet<>())
                .build();

        user = userRepository.save(user);

        // Assign departments if specified
        if (request.getAssignedDepartmentIds() != null && !request.getAssignedDepartmentIds().isEmpty()) {
            assignDepartmentsInternal(user, request.getAssignedDepartmentIds());
        }

        return convertToDTO(user);
    }

    /**
     * Update an existing user
     *
     * @param id the user ID to update
     * @param request the update request
     * @param currentUser the user making the request (for permission checking)
     * @return the updated user DTO
     */
    @Transactional
    public UserDTO updateUser(UUID id, UpdateUserRequest request, User currentUser) {
        User user = userRepository.findByIdWithDepartments(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isSelf = currentUser.getId().equals(id);

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        // Fields anyone can update on their own profile
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // Admin-only fields
        if (isAdmin) {
            log.info("Admin updating user {}: role={}, isActive={}", id, request.getRole(), request.getIsActive());
            if (request.getRole() != null) {
                user.setRole(request.getRole());
            }
            if (request.getIsActive() != null) {
                log.info("Setting isActive from {} to {}", user.isActive(), request.getIsActive());
                user.setActive(request.getIsActive());
            }
            if (request.getAssignedDepartmentIds() != null) {
                // Clear existing and reassign
                user.getAssignedDepartments().clear();
                assignDepartmentsInternal(user, request.getAssignedDepartmentIds());
            }
            if (request.getCanEditAssignedDepartments() != null) {
                user.setCanEditAssignedDepartments(request.getCanEditAssignedDepartments());
            }
        } else {
            log.info("Non-admin user {} updating their own profile", currentUser.getUsername());
        }

        // Password change (anyone can change their own, admin can change others)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (isAdmin || isSelf) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    /**
     * Delete a user
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Delete profile photo if exists
        if (user.getProfilePhotoUrl() != null) {
            fileUploadService.deleteProfilePhoto(user.getProfilePhotoUrl());
        }

        userRepository.delete(user);
    }

    /**
     * Assign departments to a user (replaces existing assignments)
     */
    @Transactional
    public UserDTO assignDepartments(UUID userId, List<String> departmentIds) {
        // 1. Fetch user with their departments (uses JOIN FETCH)
        User user = userRepository.findByIdWithDepartments(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // 2. CLEAR all existing department assignments
        user.getAssignedDepartments().clear();
        // 3. Assign new departments using internal helper
        assignDepartmentsInternal(user, departmentIds);
        // 4. Save and return
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    /**
     * Remove a single department from a user
     */
    @Transactional
    public UserDTO removeDepartment(UUID userId, String departmentId) {
        // fetch users with departments
        User user = userRepository.findByIdWithDepartments(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // remove specific department with remove if
        user.getAssignedDepartments().removeIf(d -> d.getId().equals(departmentId));
        // save and return
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    /**
     * Get all users assigned to a department
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByDepartment(String departmentId) {
        return userRepository.findByAssignedDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Upload a profile photo for a user
     */
    @Transactional
    public UserDTO uploadPhoto(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Delete old photo if exists
        if (user.getProfilePhotoUrl() != null) {
            fileUploadService.deleteProfilePhoto(user.getProfilePhotoUrl());
        }

        // Upload new photo
        String photoUrl = fileUploadService.uploadProfilePhoto(userId, file);
        user.setProfilePhotoUrl(photoUrl);
        user = userRepository.save(user);

        return convertToDTO(user);
    }

    /**
     * Get extended profile for a user
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(UUID id) {
        User user = userRepository.findByIdWithDepartments(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<DepartmentSummaryDTO> deptSummaries = user.getAssignedDepartments().stream()
                .map(d -> DepartmentSummaryDTO.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .build())
                .collect(Collectors.toList());

        // Get detailed department info
        List<DepartmentDTO> deptDetails = user.getAssignedDepartments().stream()
                .map(this::convertDepartmentToDTO)
                .collect(Collectors.toList());

        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .jobTitle(user.getJobTitle())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .isActive(user.isActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .assignedDepartments(deptSummaries)
                .assignedDepartmentsDetail(deptDetails)
                .recentEvaluations(Collections.emptyList()) // TODO: Add evaluation lookup if needed
                .build();
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findByIdWithDepartments(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return convertToDTO(user);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return convertToDTO(user);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllWithDepartments().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(UUID id) {
        return userRepository.findByIdWithDepartments(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Update last login timestamp
     */
    @Transactional
    public void updateLastLogin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Internal helper to assign departments to a user
     */
    private void assignDepartmentsInternal(User user, List<String> departmentIds) {
        for (String deptId : departmentIds) {
            // 1. Find each department by ID
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new EntityNotFoundException("Department not found: " + deptId));
            // 2. Add department to user's collection
            user.getAssignedDepartments().add(dept);
        }
    }

    /**
     * Convert User entity to DTO
     */
    private UserDTO convertToDTO(User user) {
        List<DepartmentSummaryDTO> deptSummaries = new ArrayList<>();
        if (user.getAssignedDepartments() != null) {
            deptSummaries = user.getAssignedDepartments().stream()
                    .map(d -> DepartmentSummaryDTO.builder()
                            .id(d.getId())
                            .name(d.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .jobTitle(user.getJobTitle())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .isActive(user.isActive())
                .canEditAssignedDepartments(user.isCanEditAssignedDepartments())
                .lastLogin(user.getLastLogin())
                .assignedDepartments(deptSummaries)
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Convert Department entity to DTO (simplified for profile)
     */
    private DepartmentDTO convertDepartmentToDTO(Department dept) {
        return DepartmentDTO.builder()
                .id(dept.getId())
                .name(dept.getName())
                .build();
    }

    /**
     * Get all users with their overall scores calculated from assigned departments.
     * The overall score is the average of all assigned department scores.
     * Uses finalScore if available, otherwise falls back to OKR score.
     */
    @Transactional(readOnly = true)
    public List<UserWithScoreDTO> getAllUsersWithScores() {
        try {
            return userRepository.findAllWithDepartments().stream()
                    .map(this::convertToUserWithScoreDTO)
                    .collect(Collectors.toList());
        } finally {
            scoreCalculationService.clearCache();
        }
    }

    /**
     * Convert User entity to UserWithScoreDTO with calculated overall score
     */
    private UserWithScoreDTO convertToUserWithScoreDTO(User user) {
        List<DepartmentSummaryDTO> deptSummaries = new ArrayList<>();
        List<Double> departmentScores = new ArrayList<>();

        if (user.getAssignedDepartments() != null) {
            for (Department dept : user.getAssignedDepartments()) {
                deptSummaries.add(DepartmentSummaryDTO.builder()
                        .id(dept.getId())
                        .name(dept.getName())
                        .build());

                // Calculate department score (finalScore if available, otherwise OKR score)
                DepartmentScoreResult scoreResult = scoreCalculationService
                        .calculateDepartmentScoreWithEvaluations(dept.getId(), dept.getObjectives());

                Double score = scoreResult.getFinalCombinedScore();
                if (score == null) {
                    score = scoreResult.getAutomaticOkrScore();
                }
                if (score != null && score > 0) {
                    departmentScores.add(score);
                }
            }
        }

        // Calculate overall score as average of department scores
        Double overallScore = null;
        String scoreLevel = null;
        String scoreColor = null;
        Double scorePercentage = null;

        if (!departmentScores.isEmpty()) {
            overallScore = departmentScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            overallScore = Math.round(overallScore * 100.0) / 100.0;

            // Get score level and color from the calculation service
            ScoreResult tempScore = ScoreResult.builder()
                    .score(overallScore)
                    .build();

            // Calculate level and color using a helper
            ScoreResult fullScore = scoreCalculationService.calculateDepartmentScore(Collections.emptySet());
            // Use the createScoreResult-like logic
            scoreLevel = getLevelForScore(overallScore);
            scoreColor = getColorForLevel(scoreLevel);
            scorePercentage = scoreToPercentage(overallScore);
        }

        return UserWithScoreDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .jobTitle(user.getJobTitle())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .isActive(user.isActive())
                .canEditAssignedDepartments(user.isCanEditAssignedDepartments())
                .lastLogin(user.getLastLogin())
                .assignedDepartments(deptSummaries)
                .createdAt(user.getCreatedAt())
                .overallScore(overallScore)
                .scoreLevel(scoreLevel)
                .scoreColor(scoreColor)
                .scorePercentage(scorePercentage)
                .build();
    }

    /**
     * Get score level for a given score value
     */
    private String getLevelForScore(double score) {
        if (score >= 5.00) return "exceptional";
        if (score >= 4.75) return "very_good";
        if (score >= 4.50) return "good";
        if (score >= 4.25) return "meets";
        return "below";
    }

    /**
     * Get color for a given score level
     */
    private String getColorForLevel(String level) {
        return switch (level) {
            case "exceptional" -> "#1e7b34";
            case "very_good" -> "#28a745";
            case "good" -> "#5cb85c";
            case "meets" -> "#f0ad4e";
            default -> "#d9534f";
        };
    }

    /**
     * Convert score to percentage (based on 3.0-5.0 range)
     */
    private double scoreToPercentage(double score) {
        double minScore = 3.0;
        double maxScore = 5.0;
        double range = maxScore - minScore;
        return Math.round(((score - minScore) / range) * 1000.0) / 10.0;
    }
}
