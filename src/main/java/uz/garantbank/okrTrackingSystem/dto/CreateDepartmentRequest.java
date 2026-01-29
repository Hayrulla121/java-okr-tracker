package uz.garantbank.okrTrackingSystem.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class CreateDepartmentRequest {
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    @NotBlank(message = "Division ID is required")
    private String divisionId;  // Required field
}

