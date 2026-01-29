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
 * DTO for User information with overall score calculated from assigned departments.
 * Used for the Team Overview page where Directors can view all users with their scores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithScoreDTO {
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

    @JsonProperty("canEditAssignedDepartments")
    private boolean canEditAssignedDepartments;

    private LocalDateTime lastLogin;
    private List<DepartmentSummaryDTO> assignedDepartments;
    private LocalDateTime createdAt;

    /**
     * Overall score calculated as the average of all assigned department scores
     * (uses finalScore if available, otherwise falls back to OKR score)
     */
    private Double overallScore;

    /**
     * The score level name (e.g., "exceptional", "very_good", "good", "meets", "below")
     */
    private String scoreLevel;

    /**
     * The color associated with the score level
     */
    private String scoreColor;

    /**
     * The score as a percentage (0-100)
     */
    private Double scorePercentage;
}
