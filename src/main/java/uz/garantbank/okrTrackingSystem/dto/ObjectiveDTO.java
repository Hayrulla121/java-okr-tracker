package uz.garantbank.okrTrackingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectiveDTO {
    private String id;
    private String name;
    private Integer weight;
    private String departmentId;
    private List<KeyResultDTO> keyResults;
    private ScoreResult score; // Computed field
}