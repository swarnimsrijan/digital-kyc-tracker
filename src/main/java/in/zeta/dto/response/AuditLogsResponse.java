package in.zeta.dto.response;

import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
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
public class AuditLogsResponse {
    private UUID id;
    private EntityType entityType;
    private UUID entityId;
    private AuditAction action;
    private UUID userId;
    private String username;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
}
