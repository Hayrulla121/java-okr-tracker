package uz.garantbank.okrTrackingSystem.dto;
import lombok.*;
import uz.garantbank.okrTrackingSystem.entity.KeyResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyResultDTO {
    private String id;
    private String name;
    private String description;
    private KeyResult.MetricType metricType;
    private String unit;
    private Integer weight;
    private ThresholdDTO thresholds;
    private String actualValue;
    private String objectiveId;
    private ScoreResult score; // Computed field
}

