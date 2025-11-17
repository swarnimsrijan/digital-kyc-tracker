package in.zeta.dto.requests.events;

import in.zeta.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdatedEvent {
    AuditAction commentAction;
    private UUID commentId;
    private UUID verificationRequestId;
    private UUID updatedBy;
    private String oldCommentText;
    private String newCommentText;
    private LocalDateTime updatedAt;
}

