package in.zeta.service.impl;

import in.zeta.entity.AuditLogs;
import in.zeta.entity.Users;
import in.zeta.enums.EntityType;
import in.zeta.enums.AuditAction;
import in.zeta.exception.JsonParsingException;
import in.zeta.mapper.AuditLogMapper;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.exception.AuditLogException;
import in.zeta.exception.InvalidOperationException;
import in.zeta.producer.EventProducer;
import in.zeta.repository.AuditLogRepository;
import in.zeta.dto.response.AuditLogsResponse;
import in.zeta.service.AuditService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import in.zeta.service.UserService;

import static in.zeta.mapper.AuditLogMapper.toResponse;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    @Value("${atropos.audit.topic}")
    private String auditLogTopic;

    private final AuditLogRepository auditLogRepository;
    private final EventProducer eventProducer;
    private final UserService userService;

    private static final SpectraLogger logger = OlympusSpectra.getLogger(AuditServiceImpl.class);

    @Override
    public List<AuditLogsResponse> getAuditTrail(EntityType entityType, UUID entityId) {
        if (entityType == null || entityId == null) {
            throw new InvalidOperationException("EntityType and EntityId cannot be null for audit trail");
        }

        logger.info("Fetching audit trail")
                .attr("entityType", entityType)
                .attr("entityId", entityId)
                .log();

        List<AuditLogs> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);

        return auditLogs.stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }


    @Override
    public void createAuditLog(EntityType entityType, UUID entityId, AuditAction action,
                               Users user, String oldValue, String newValue) {

        if (entityType == null || entityId == null || action == null) {
            throw new InvalidOperationException("EntityType, EntityId, and Action are required for audit log");
        }

        try {
            AuditLogs auditLog = AuditLogs.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .user(user)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            logger.info("Audit log created successfully")
                    .attr("entityType", entityType)
                    .attr("entityId", entityId)
                    .attr("action", action)
                    .attr("user", user.getId().toString())
                    .attr("oldValue", oldValue)
                    .attr("newValue", newValue)
                    .attr("timestamp", auditLog.getTimestamp().toString())
                    .log();

        } catch (Exception e) {

            logger.error("Failed to create audit log")
                    .attr("entityType", entityType)
                    .attr("entityId", entityId)
                    .attr("action", action)
                    .attr("error", e.getMessage())
                    .log();

            throw new AuditLogException(
                    String.format("Failed to create audit log for %s:%s with action %s",
                            entityType, entityId, action), e);
        }
    }

    @Override
    public List<AuditLogsResponse> getAuditLogsByAction(AuditAction action) {
        if (action == null) {
            throw new InvalidOperationException("Action cannot be null for audit logs query");
        }

        logger.info("Retrieving audit logs for action")
                .attr("action", action)
                .log();

        List<AuditLogs> auditLogs = auditLogRepository.findByActionOrderByTimestampDesc(action);

        return auditLogs.stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }

    @Override
    public List<AuditLogsResponse> getAuditLogsByUser(UUID userId) {
        if (userId == null) {
            throw new InvalidOperationException("User ID cannot be null for audit logs query");
        }

        logger.info("Retrieving audit logs for user")
                .attr("userId", userId)
                .log();

        List<AuditLogs> auditLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        return auditLogs.stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }

    @Override
    public List<AuditLogsResponse> getAllAuditLogs() {

        logger.info("Retrieving all audit logs")
                .log();

        List<AuditLogs> auditLogs = auditLogRepository.findAllByOrderByTimestampDesc();

        return auditLogs.stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }

    @Override
    public void publishAuditLogEvent(AuditLogCreatedEvent event) {
        eventProducer.publishEvent(
                event.getEntityType().toString(),
                event.getEntityId().toString(),
                auditLogTopic,
                event
        );
    }


    @Override
    public AuditLogsResponse getAuditLogById(UUID auditLogId) {
        if (auditLogId == null) {
            throw new InvalidOperationException("Audit Log ID cannot be null");
        }

        logger.info("Fetching audit log by ID")
                .attr("auditLogId", auditLogId)
                .log();

        AuditLogs auditLog = auditLogRepository.findById(auditLogId)
                .orElse(null);

        if (auditLog == null) {
            logger.error("Audit log not found")
                    .attr("auditLogId", auditLogId)
                    .log();
            return null;
        }

        return toResponse(auditLog);
    }

    @Override
    public void processAuditLogEvent(String eventPayload) {
        try {
            AuditLogCreatedEvent event = JsonUtil.parseAuditLogCreatedEvent(eventPayload);
            Users user = userService.getByIdOrThrow(event.getUserId(), "User not found for audit log event");

            logger.info("Processed audit log event successfully")
                    .attr("entityType", event.getEntityType())
                    .attr("entityId", event.getEntityId())
                    .attr("action", event.getAction())
                    .log();

            auditLogRepository.save(AuditLogMapper.toEntity(event,user));

        } catch (JsonParsingException e) {
            logger.error("Failed to parse audit log event")
                    .attr("event", eventPayload)
                    .attr("error", e.getMessage())
                    .log();
            throw new AuditLogException("Failed to parse audit log event", e);
        } catch (Exception e) {
            logger.error("Failed to process audit log event")
                    .attr("event", eventPayload)
                    .attr("error", e.getMessage())
                    .log();

            throw new AuditLogException("Failed to process audit log event", e);
        }
    }
}