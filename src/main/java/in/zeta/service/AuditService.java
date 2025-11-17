package in.zeta.service;

import in.zeta.entity.Users;
import in.zeta.enums.EntityType;
import in.zeta.enums.AuditAction;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.response.AuditLogsResponse;

import java.util.List;
import java.util.UUID;

public interface AuditService {
    List<AuditLogsResponse> getAuditTrail(EntityType entityType, UUID entityId);
    void createAuditLog(EntityType entityType, UUID entityId, AuditAction action,
                        Users user, String oldValue, String newValue);
    List<AuditLogsResponse> getAuditLogsByAction(AuditAction action);
    List<AuditLogsResponse> getAuditLogsByUser(UUID userId);
    List<AuditLogsResponse> getAllAuditLogs();
    void publishAuditLogEvent(AuditLogCreatedEvent auditLogCreatedEvent);
    AuditLogsResponse getAuditLogById(UUID auditLogId);
    void processAuditLogEvent(String eventPayload);
}