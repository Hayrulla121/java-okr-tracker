package uz.garantbank.okrTrackingSystem.dto.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.garantbank.okrTrackingSystem.entity.Role;

import java.util.List;

/**
 * Request DTO for updating an existing user.
 * All fields are optional - only provided fields will be updated.
 * Some fields (role, assignedDepartmentIds, isActive) can only be updated by ADMIN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    /**
     * User's full display name
     */
    private String fullName;

    /**
     * User's email address
     */
    @Email(message = "Email must be valid")
    private String email;

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
     * User's role (ADMIN only)
     */
    private Role role;

    /**
     * List of department IDs to assign (ADMIN only)
     */
    private List<String> assignedDepartmentIds;

    /**
     * Whether the user account is active (ADMIN only)
     */
    @JsonProperty("isActive")
    private Boolean isActive;

    /**
     * Whether the employee can edit their assigned departments (ADMIN only)
     */
    @JsonProperty("canEditAssignedDepartments")
    private Boolean canEditAssignedDepartments;

    /**
     * New password (optional, for password change)
     */
    private String password;
}
