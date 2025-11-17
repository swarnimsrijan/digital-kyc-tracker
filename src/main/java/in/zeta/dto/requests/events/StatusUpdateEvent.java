package in.zeta.dto.requests.events;

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
public class StatusUpdateEvent {
    private UUID id;
    private UUID verificationRequestId;
    private VerificationStatus fromStatus;
    private VerificationStatus toStatus;
    private UUID changedBy;
    private String reason;
    private LocalDateTime changedAt;
}