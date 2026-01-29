package uz.garantbank.okrTrackingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.garantbank.okrTrackingSystem.dto.user.DepartmentSummaryDTO;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DivisionDTO {
    private String id;
    private String name;
    private UserSummaryDTO divisionLeader;  // Only basic leader info
    private List<DepartmentSummaryDTO> departments;  // Only department summaries
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors, getters, setters...
}

