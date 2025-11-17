package in.zeta.dto.response;

import in.zeta.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequestResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private UUID requestorId;
    private String requestorName;
    private UUID assignedOfficerId;
    private String assignedOfficerName;
    private VerificationStatus status;
    private String requestReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private List<DocumentResponse> documents;
    private List<CommentResponse> comments;
}