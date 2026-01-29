package uz.garantbank.okrTrackingSystem.dto.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.garantbank.okrTrackingSystem.entity.Role;

import java.util.List;

/**
 * Request DTO for creating a new user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Role is required")
    private Role role;

    /**
     * List of department IDs to assign to the user
     */
    private List<String> assignedDepartmentIds;

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
}