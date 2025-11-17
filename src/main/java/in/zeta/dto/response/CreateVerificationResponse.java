package in.zeta.dto.response;

import in.zeta.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVerificationResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private UUID requestorId;
    private String requestorName;
    private VerificationStatus status;
    private String requestReason;
    private LocalDateTime createdAt;
}