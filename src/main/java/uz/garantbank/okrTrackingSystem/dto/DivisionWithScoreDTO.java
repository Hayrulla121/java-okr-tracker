package uz.garantbank.okrTrackingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Division with aggregated score from its departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionWithScoreDTO {
    @Schema(description = "Division ID", example = "div-001")
    private String id;

    @Schema(description = "Division name", example = "Information Technology")
    private String name;

    @Schema(description = "Aggregated score (3.0-5.0)", example = "4.35")
    private Double score;

    @Schema(description = "Score level classification", example = "good", allowableValues = {"below", "meets", "good", "very_good", "exceptional"})
    private String scoreLevel;

    @Schema(description = "Hex color for score level", example = "#4CAF50")
    private String color;

    @Schema(description = "Score as percentage (0-100)", example = "67.5")
    private Double percentage;
}
