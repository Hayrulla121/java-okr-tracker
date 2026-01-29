package uz.garantbank.okrTrackingSystem.dto;

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
public class UpdateDivisionRequest {
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;  // Optional - only update if provided

    private UUID leaderId;  // Optional

    // Getters, setters...
}

