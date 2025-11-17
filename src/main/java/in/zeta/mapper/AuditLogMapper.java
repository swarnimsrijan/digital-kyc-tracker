package in.zeta.mapper;

import com.google.gson.*;
import in.zeta.entity.AuditLogs;
import in.zeta.entity.Users;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.response.AuditLogsResponse;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogMapper {

    private AuditLogMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static AuditLogCreatedEvent createAuditLogEvent(
            EntityType entityType,
            UUID entityId,
            AuditAction action,
            Users user,
            String oldValue,
            String newValue
    ) {
        return AuditLogCreatedEvent.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .userId(user.getId())
                .username(user.getUsername())
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();
    }
    public static AuditLogsResponse toResponse(AuditLogs log) {
        return AuditLogsResponse.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUser() != null ? log.getUser().getUsername() : null)
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .timestamp(log.getTimestamp())
                .build();
    };
    public static AuditLogs toEntity(AuditLogCreatedEvent event, Users user) {
        AuditLogs auditLog = new AuditLogs();
        auditLog.setEntityType(event.getEntityType());
        auditLog.setEntityId(event.getEntityId());
        auditLog.setAction(event.getAction());
        auditLog.setUser(user);
        auditLog.setOldValue(event.getOldValue());
        auditLog.setNewValue(event.getNewValue());
        auditLog.setTimestamp(event.getTimestamp());
        return auditLog;
    }
}
