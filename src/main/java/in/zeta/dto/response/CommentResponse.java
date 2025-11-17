package in.zeta.dto.response;

import in.zeta.enums.AuditAction;
import in.zeta.enums.CommentType;
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
public class CommentResponse {
    private AuditAction commentAction;
    private UUID id;
    private UUID verificationRequestId;
    private String userName;
    private String commentText;
    private CommentType commentType;
    private LocalDateTime createdAt;
    private Boolean isRead;
    private Boolean isEdited;
}