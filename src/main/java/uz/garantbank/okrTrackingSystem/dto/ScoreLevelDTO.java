package uz.garantbank.okrTrackingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Score level configuration entry")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreLevelDTO {
    @Schema(description = "Score level ID", example = "sl-001")
    private String id;

    @Schema(description = "Level name", example = "Good", allowableValues = {"Below", "Meets", "Good", "Very Good", "Exceptional"})
    private String name;

    @Schema(description = "Numeric score value for this level", example = "4.5")
    private Double scoreValue;

    @Schema(description = "Hex color code for UI display", example = "#4CAF50")
    private String color;

    @Schema(description = "Display order (lower = displayed first)", example = "3")
    private Integer displayOrder;
}
