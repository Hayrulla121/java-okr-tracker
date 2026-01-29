package uz.garantbank.okrTrackingSystem.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.garantbank.okrTrackingSystem.dto.DepartmentDTO;
import uz.garantbank.okrTrackingSystem.dto.EvaluationDTO;
import uz.garantbank.okrTrackingSystem.entity.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Extended DTO for user profile, including detailed department info and evaluations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private String profilePhotoUrl;
    private String jobTitle;
    private String phoneNumber;
    private String bio;
    @JsonProperty("isActive")
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    /**
     * Summary list of assigned departments
     */
    private List<DepartmentSummaryDTO> assignedDepartments;

    /**
     * Detailed information about assigned departments
     */
    private List<DepartmentDTO> assignedDepartmentsDetail;

    /**
     * Recent evaluations for this user (for EMPLOYEE role)
     */
    private List<EvaluationDTO> recentEvaluations;
}
