package uz.garantbank.okrTrackingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionWithScoreDTO {
    private String id;
    private String name;
    private Double score;
    private String scoreLevel;
    private String color;
    private Double percentage;
}