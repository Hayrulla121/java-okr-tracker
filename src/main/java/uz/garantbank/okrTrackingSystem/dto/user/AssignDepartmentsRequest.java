package uz.garantbank.okrTrackingSystem.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for assigning departments to a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDepartmentsRequest {

    /**
     * List of department IDs to assign to the user
     */
    @NotNull(message = "Department IDs are required")
    private List<String> departmentIds;
}
