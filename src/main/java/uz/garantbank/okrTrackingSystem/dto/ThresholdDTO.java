package uz.garantbank.okrTrackingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Threshold values defining score level boundaries for a Key Result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThresholdDTO {
    @Schema(description = "Value at or below which score is 'Below' (3.0)", example = "90.0")
    private Double below;

    @Schema(description = "Value at which score is 'Meets' (4.25)", example = "95.0")
    private Double meets;

    @Schema(description = "Value at which score is 'Good' (4.5)", example = "97.0")
    private Double good;

    @Schema(description = "Value at which score is 'Very Good' (4.75)", example = "98.5")
    private Double veryGood;

    @Schema(description = "Value at which score is 'Exceptional' (5.0)", example = "99.5")
    private Double exceptional;
}
