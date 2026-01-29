package uz.garantbank.okrTrackingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class DivisionSummaryDTO {
    private String id;
    private String name;

    // Minimal info - used when you just need division name

    public DivisionSummaryDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters, setters...
}

