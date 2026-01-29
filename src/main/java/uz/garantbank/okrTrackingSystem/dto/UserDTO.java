package uz.garantbank.okrTrackingSystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.garantbank.okrTrackingSystem.dto.user.DepartmentSummaryDTO;
import uz.garantbank.okrTrackingSystem.entity.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for User information (excluding password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private Role role;

    /**
     * URL to user's profile photo
     */
    private String profilePhotoUrl;

    /**
     * User's job title
     */
    private String jobTitle;

    /**
     * User's phone number
     */
    private String phoneNumber;

    /**
     * User's bio/description
     */
    private String bio;

    /**
     * Whether the user account is active
     */
    @JsonProperty("isActive")
    private boolean isActive;

    /**
     * Whether the employee can edit their assigned departments
     */
    @JsonProperty("canEditAssignedDepartments")
    private boolean canEditAssignedDepartments;

    /**
     * Timestamp of user's last login
     */
    private LocalDateTime lastLogin;

    /**
     * List of departments the user is assigned to
     */
    private List<DepartmentSummaryDTO> assignedDepartments;

    /**
     * Timestamp when user was created
     */
    private LocalDateTime createdAt;
}
