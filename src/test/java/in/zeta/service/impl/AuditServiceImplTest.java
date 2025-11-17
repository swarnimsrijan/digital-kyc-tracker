package in.zeta.service.impl;

import in.zeta.entity.AuditLogs;
import in.zeta.entity.Users;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.response.AuditLogsResponse;
import in.zeta.enums.Role;
import in.zeta.exception.AuditLogException;
import in.zeta.exception.InvalidOperationException;
import in.zeta.producer.EventProducer;
import in.zeta.repository.AuditLogRepository;
import in.zeta.service.UserService;
import in.zeta.spectra.capture.SpectraLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserService userService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private SpectraLogger logger;

    @InjectMocks
    private AuditServiceImpl auditService;

    private UUID testUserId;
    private UUID testAuditLogId;
    private Users testUser;
    private AuditLogs testAuditLog;
    private AuditLogCreatedEvent testEvent;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testAuditLogId = UUID.randomUUID();

        testUser = Users.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .build();

        testAuditLog = AuditLogs.builder()
                .id(testAuditLogId)
                .entityType(EntityType.USER)
                .entityId(testUserId)
                .action(AuditAction.USER_CREATED)
                .user(testUser)
                .timestamp(LocalDateTime.now())
                .oldValue("old")
                .newValue("new")
                .build();

        testEvent = AuditLogCreatedEvent.builder()
                .entityType(EntityType.USER)
                .entityId(testUserId)
                .action(AuditAction.USER_CREATED)
                .userId(testUserId)
                .oldValue("old")
                .newValue("new")
                .timestamp(LocalDateTime.now())
                .build();

        ReflectionTestUtils.setField(auditService, "auditLogTopic", "audit-topic");
    }

    @Test
    void createAuditLog_Success() {
        // Given
        when(auditLogRepository.save(any(AuditLogs.class))).thenReturn(testAuditLog);

        // When
        auditService.createAuditLog(
                testEvent.getEntityType(),
                testEvent.getEntityId(),
                testEvent.getAction(),
                testUser,
                testEvent.getOldValue(),
                testEvent.getNewValue()
        );

        // Then
        verify(auditLogRepository).save(any(AuditLogs.class));
    }

    @Test
    void createAuditLog_Exception() {
        // Given
        when(auditLogRepository.save(any(AuditLogs.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(AuditLogException.class, () -> auditService.createAuditLog(
                testEvent.getEntityType(),
                testEvent.getEntityId(),
                testEvent.getAction(),
                testUser,
                testEvent.getOldValue(),
                testEvent.getNewValue()
        ));
    }

    @Test
    void getAuditLogsByAction_Success() {
        // Given
        List<AuditLogs> auditLogs = Arrays.asList(testAuditLog);
        when(auditLogRepository.findByActionOrderByTimestampDesc(AuditAction.USER_CREATED))
                .thenReturn(auditLogs);

        // When
        List<AuditLogsResponse> result = auditService.getAuditLogsByAction(AuditAction.USER_CREATED);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository).findByActionOrderByTimestampDesc(AuditAction.USER_CREATED);
    }

    @Test
    void getAuditLogsByAction_NullAction() {
        // When & Then
        assertThrows(InvalidOperationException.class,
                () -> auditService.getAuditLogsByAction(null));
    }

    @Test
    void getAuditLogsByAction_EmptyList() {
        // Given
        when(auditLogRepository.findByActionOrderByTimestampDesc(AuditAction.USER_CREATED))
                .thenReturn(Arrays.asList());

        // When
        List<AuditLogsResponse> result = auditService.getAuditLogsByAction(AuditAction.USER_CREATED);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByActionOrderByTimestampDesc(AuditAction.USER_CREATED);
    }

    @Test
    void getAuditLogsByUser_Success() {
        // Given
        List<AuditLogs> auditLogs = Arrays.asList(testAuditLog);
        when(auditLogRepository.findByUserIdOrderByTimestampDesc(testUserId))
                .thenReturn(auditLogs);

        // When
        List<AuditLogsResponse> result = auditService.getAuditLogsByUser(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository).findByUserIdOrderByTimestampDesc(testUserId);
    }

    @Test
    void getAuditLogsByUser_NullUserId() {
        // When & Then
        assertThrows(InvalidOperationException.class,
                () -> auditService.getAuditLogsByUser(null));
    }

    @Test
    void getAuditLogsByUser_EmptyList() {
        // Given
        when(auditLogRepository.findByUserIdOrderByTimestampDesc(testUserId))
                .thenReturn(Arrays.asList());

        // When
        List<AuditLogsResponse> result = auditService.getAuditLogsByUser(testUserId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByUserIdOrderByTimestampDesc(testUserId);
    }

    @Test
    void getAllAuditLogs_Success() {
        // Given
        List<AuditLogs> auditLogs = Arrays.asList(testAuditLog);
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(auditLogs);

        // When
        List<AuditLogsResponse> result = auditService.getAllAuditLogs();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    void getAuditLogById_Success() {
        // Given
        when(auditLogRepository.findById(testAuditLogId)).thenReturn(Optional.of(testAuditLog));

        // When
        AuditLogsResponse result = auditService.getAuditLogById(testAuditLogId);

        // Then
        assertNotNull(result);
        assertEquals(testAuditLogId, result.getId());
        verify(auditLogRepository).findById(testAuditLogId);
    }

    @Test
    void getAuditLogById_NotFound() {
        // Given
        when(auditLogRepository.findById(testAuditLogId)).thenReturn(Optional.empty());

        // When
        AuditLogsResponse result = auditService.getAuditLogById(testAuditLogId);

        // Then
        assertNull(result);
        verify(auditLogRepository).findById(testAuditLogId);
    }

    @Test
    void getAuditLogById_NullId() {
        // When & Then
        assertThrows(InvalidOperationException.class,
                () -> auditService.getAuditLogById(null));
    }

    @Test
    void publishAuditLogEvent_Success() {
        // When
        auditService.publishAuditLogEvent(testEvent);

        // Then
        verify(eventProducer).publishEvent(
                testEvent.getEntityType().toString(),
                testEvent.getEntityId().toString(),
                "audit-topic",
                testEvent
        );
    }

    @Test
    void publishAuditLogEvent_NullEvent() {
        // When & Then
        assertThrows(NullPointerException.class,
                () -> auditService.publishAuditLogEvent(null));
        verify(eventProducer, never()).publishEvent(any(), any(), any(), any());
    }
}