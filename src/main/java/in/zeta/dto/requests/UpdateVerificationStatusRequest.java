package in.zeta.dto.requests;


import in.zeta.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateVerificationStatusRequest {
    @NotNull(message = "Status is required")
    private VerificationStatus status;
    private String reason;
}