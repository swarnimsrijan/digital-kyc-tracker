package in.zeta.mapper;

import in.zeta.entity.AuditLogs;
import in.zeta.entity.Users;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.response.AuditLogsResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogMapperTest {

    @Test
    void createAuditLogEvent_success() {
        Users user = Users.builder().id(UUID.randomUUID()).username("user").build();
        UUID entityId = UUID.randomUUID();
        AuditLogCreatedEvent event = AuditLogMapper.createAuditLogEvent(
                EntityType.DOCUMENT, entityId, AuditAction.DOCUMENT_UPLOADED, user, "old", "new"
        );

        assertNotNull(event);
        assertEquals(EntityType.DOCUMENT, event.getEntityType());
        assertEquals(entityId, event.getEntityId());
        assertEquals(AuditAction.DOCUMENT_UPLOADED, event.getAction());
        assertEquals(user.getId(), event.getUserId());
        assertEquals(user.getUsername(), event.getUsername());
        assertEquals("old", event.getOldValue());
        assertEquals("new", event.getNewValue());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void toResponse_success() {
        Users user = Users.builder().id(UUID.randomUUID()).username("user").build();
        AuditLogs log = AuditLogs.builder()
                .id(UUID.randomUUID())
                .entityType(EntityType.DOCUMENT)
                .entityId(UUID.randomUUID())
                .action(AuditAction.DOCUMENT_UPLOADED)
                .user(user)
                .oldValue("old")
                .newValue("new")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLogsResponse response = AuditLogMapper.toResponse(log);

        assertNotNull(response);
        assertEquals(log.getId(), response.getId());
        assertEquals(log.getEntityType(), response.getEntityType());
        assertEquals(log.getEntityId(), response.getEntityId());
        assertEquals(log.getAction(), response.getAction());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(log.getOldValue(), response.getOldValue());
        assertEquals(log.getNewValue(), response.getNewValue());
        assertEquals(log.getTimestamp(), response.getTimestamp());
    }

    @Test
    void toResponse_nullUser_success() {
        AuditLogs log = AuditLogs.builder()
                .id(UUID.randomUUID())
                .entityType(EntityType.DOCUMENT)
                .entityId(UUID.randomUUID())
                .action(AuditAction.DOCUMENT_UPLOADED)
                .user(null)
                .oldValue("old")
                .newValue("new")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLogsResponse response = AuditLogMapper.toResponse(log);

        assertNull(response.getUserId());
        assertNull(response.getUsername());
    }
}