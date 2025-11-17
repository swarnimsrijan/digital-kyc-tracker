package in.zeta.dto.requests.events;

import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogCreatedEvent {
    private EntityType entityType;
    private UUID entityId;
    private AuditAction action;
    private UUID userId;
    private String username;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
}
