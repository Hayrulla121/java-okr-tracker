package uz.garantbank.okrTrackingSystem.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionScoreResult {
    // Automatic OKR score (calculated from departments)
    private Double automaticOkrScore;
    private Double automaticOkrPercentage;

    // Director evaluation (if applicable)
    private Double directorEvaluation;
    private Integer directorStars;
    private String directorComment;

    // Final combined score
    private Double finalCombinedScore;
    private Double finalPercentage;

    // Score level and color
    private String scoreLevel;
    private String color;

    // Flags for which evaluations exist
    private Boolean hasDirectorEvaluation;
}
