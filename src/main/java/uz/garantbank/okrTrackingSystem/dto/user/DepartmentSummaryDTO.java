package uz.garantbank.okrTrackingSystem.dto.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for department information in user contexts.
 * Contains only essential department data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSummaryDTO {

    /**
     * Department ID
     */
    private String id;

    /**
     * Department name
     */
    private String name;
}

