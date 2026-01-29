package uz.garantbank.okrTrackingSystem.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDivisionRequest {
    @NotBlank(message = "Division name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    private UUID leaderId;  // Optional

    // Getters, setters...
}
