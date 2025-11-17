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
public class StatusHistoryResponse {
    private UUID id;
    private UUID verificationRequestId;
    private VerificationStatus fromStatus;
    private VerificationStatus toStatus;
    private UUID changedBy;
    private String reason;
    private LocalDateTime changedAt;
}
