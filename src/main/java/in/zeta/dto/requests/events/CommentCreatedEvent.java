package in.zeta.dto.requests.events;

import in.zeta.enums.AuditAction;
import in.zeta.enums.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEvent{
    AuditAction commentAction;
    private UUID commentId;
    private UUID verificationRequestId;
    private UUID createdBy;
    private String commentText;
    private CommentType commentType;
    private LocalDateTime createdAt;
}