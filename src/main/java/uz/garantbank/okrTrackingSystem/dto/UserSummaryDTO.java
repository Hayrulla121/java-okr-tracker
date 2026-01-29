package uz.garantbank.okrTrackingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {
    private UUID id;
    private String username;
    private String fullName;
    private String profilePhotoUrl;


    // Just basic user info for display

    // Constructors, getters, setters...
}
