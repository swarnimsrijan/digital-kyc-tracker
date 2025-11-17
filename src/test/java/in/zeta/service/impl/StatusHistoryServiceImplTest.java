package in.zeta.service.impl;

import in.zeta.entity.StatusHistory;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.*;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.dto.requests.events.StatusUpdateEvent;
import in.zeta.dto.response.StatusHistoryResponse;
import in.zeta.exception.DataNotFoundException;
import in.zeta.exception.ResourceNotFoundException;
import in.zeta.producer.EventProducer;
import in.zeta.repository.StatusHistoryRepository;
import in.zeta.service.AuditService;
import in.zeta.service.NotificationService;
import in.zeta.service.UserService;
import in.zeta.service.VerificationRequestService;
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
class StatusHistoryServiceImplTest {

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private VerificationRequestService verificationRequestService;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private StatusHistoryServiceImpl statusHistoryService;

    private UUID testStatusHistoryId;
    private UUID testVerificationId;
    private UUID testUserId;
    private UUID testCustomerId;
    private UUID testOfficerId;
    private StatusHistory testStatusHistory;
    private VerificationRequest testVerificationRequest;
    private Users testUser;
    private Users testCustomer;
    private Users testOfficer;
    private StatusUpdateEvent testStatusUpdateEvent;

    @BeforeEach
    void setUp() {
        testStatusHistoryId = UUID.randomUUID();
        testVerificationId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        testOfficerId = UUID.randomUUID();

        testUser = Users.builder()
                .id(testUserId)
                .email("test@example.com")
                .username("Test")
                .role(Role.CUSTOMER)
                .build();

        testCustomer = Users.builder()
                .id(testCustomerId)
                .email("customer@example.com")
                .username("Test")
                .role(Role.CUSTOMER)
                .build();

        testOfficer = Users.builder()
                .id(testOfficerId)
                .email("officer@example.com")
                .username("Test")
                .role(Role.VERIFICATION_OFFICER)
                .build();

        testVerificationRequest = VerificationRequest.builder()
                .id(testVerificationId)
                .customer(testCustomer)
                .assignedOfficer(testOfficer)
                .status(VerificationStatus.PENDING)
                .build();

        testStatusHistory = StatusHistory.builder()
                .id(testStatusHistoryId)
                .verificationRequest(testVerificationRequest)
                .fromStatus(VerificationStatus.PENDING)
                .toStatus(VerificationStatus.APPROVED)
                .changedBy(testUser)
                .changedAt(LocalDateTime.now())
                .reason("Test reason")
                .build();

        testStatusUpdateEvent = StatusUpdateEvent.builder()
                .id(testStatusHistoryId)
                .verificationRequestId(testVerificationId)
                .changedBy(testUserId)
                .fromStatus(VerificationStatus.PENDING)
                .toStatus(VerificationStatus.APPROVED)
                .reason("Test reason")
                .build();

        ReflectionTestUtils.setField(statusHistoryService, "statusUpdateTopic", "status-topic");
    }

    @Test
    void getStatusHistoryById_Success() {
        // Given
        when(statusHistoryRepository.findById(testStatusHistoryId)).thenReturn(Optional.of(testStatusHistory));

        // When
        StatusHistoryResponse result = statusHistoryService.getStatusHistoryById(testStatusHistoryId);

        // Then
        assertNotNull(result);
        verify(statusHistoryRepository).findById(testStatusHistoryId);
    }

    @Test
    void getStatusHistoryById_NotFound() {
        // Given
        when(statusHistoryRepository.findById(testStatusHistoryId)).thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> statusHistoryService.getStatusHistoryById(testStatusHistoryId));

        // Verify the exception message contains the expected components
        assertTrue(exception.getMessage().contains("status_history not found") &&
                exception.getMessage().contains("id") &&
                exception.getMessage().contains(testStatusHistoryId.toString()));

        verify(statusHistoryRepository).findById(testStatusHistoryId);
    }

    @Test
    void getLatestStatus_Success() {
        // Given
        when(verificationRequestService.getByIdOrThrow(testVerificationId)).thenReturn(testVerificationRequest);
        when(statusHistoryRepository.findLatestStatusHistory(testVerificationRequest))
                .thenReturn(Optional.of(testStatusHistory));

        // When
        VerificationStatus result = statusHistoryService.getLatestStatus(testVerificationId);

        // Then
        assertEquals(VerificationStatus.APPROVED, result);
        verify(statusHistoryRepository).findLatestStatusHistory(testVerificationRequest);
    }

    @Test
    void getLatestStatus_NotFound() {
        // Given
        UUID verificationRequestId = UUID.randomUUID();
        VerificationRequest mockVerificationRequest = new VerificationRequest();
        mockVerificationRequest.setId(verificationRequestId);

        when(verificationRequestService.getByIdOrThrow(verificationRequestId))
                .thenReturn(mockVerificationRequest);
        when(statusHistoryRepository.findLatestStatusHistory(mockVerificationRequest))
                .thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> statusHistoryService.getLatestStatus(verificationRequestId));

        // The DataNotFoundException constructor takes (tableName, fieldName, fieldValue)
        assertTrue(exception.getMessage().contains("status_history not found") &&
                exception.getMessage().contains("verification_request_id") &&
                exception.getMessage().contains(verificationRequestId.toString()));

        verify(verificationRequestService).getByIdOrThrow(verificationRequestId);
        verify(statusHistoryRepository).findLatestStatusHistory(mockVerificationRequest);
    }

    @Test
    void getStatusHistoryByVerificationId_Success() {
        // Given
        List<StatusHistory> historyList = Arrays.asList(testStatusHistory);
        when(verificationRequestService.getByIdOrThrow(testVerificationId)).thenReturn(testVerificationRequest);
        when(statusHistoryRepository.findByVerificationRequestOrderByChangedAtDesc(testVerificationRequest))
                .thenReturn(historyList);

        // When
        List<StatusHistoryResponse> result = statusHistoryService.getStatusHistoryByVerificationId(testVerificationId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(statusHistoryRepository).findByVerificationRequestOrderByChangedAtDesc(testVerificationRequest);
    }

    @Test
    void publishStatusUpdateEvent_Success() {
        // When
        statusHistoryService.publishStatusUpdateEvent(testStatusUpdateEvent);

        // Then
        verify(eventProducer).publishEvent(
                EntityType.STATUS_HISTORY.toString(),
                testStatusUpdateEvent.getId().toString(),
                "status-topic",
                testStatusUpdateEvent
        );
    }

    @Test
    void updateStatusHistory_PendingStatus() {
        // Given
        testStatusUpdateEvent.setToStatus(VerificationStatus.PENDING);
        when(verificationRequestService.getByIdOrThrow(testVerificationId)).thenReturn(testVerificationRequest);
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(statusHistoryRepository.save(any(StatusHistory.class))).thenReturn(testStatusHistory);

        // When
        statusHistoryService.updateStatusHistory(testStatusUpdateEvent);

        // Then
        verify(statusHistoryRepository).save(any(StatusHistory.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
        verify(notificationService).publishNotificationEvent(any(NotificationCreatedEvent.class));
    }

    @Test
    void updateStatusHistory_ApprovedStatus() {
        // Given
        testStatusUpdateEvent.setToStatus(VerificationStatus.APPROVED);
        when(verificationRequestService.getByIdOrThrow(testVerificationId)).thenReturn(testVerificationRequest);
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(statusHistoryRepository.save(any(StatusHistory.class))).thenReturn(testStatusHistory);

        // When
        statusHistoryService.updateStatusHistory(testStatusUpdateEvent);

        // Then
        verify(statusHistoryRepository).save(any(StatusHistory.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
        verify(notificationService).publishNotificationEvent(any(NotificationCreatedEvent.class));
        assertNotNull(testVerificationRequest.getApprovedAt());
    }

    @Test
    void updateStatusHistory_RejectedStatus() {
        // Given
        testStatusUpdateEvent.setToStatus(VerificationStatus.REJECTED);
        when(verificationRequestService.getByIdOrThrow(testVerificationId)).thenReturn(testVerificationRequest);
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(statusHistoryRepository.save(any(StatusHistory.class))).thenReturn(testStatusHistory);

        // When
        statusHistoryService.updateStatusHistory(testStatusUpdateEvent);

        // Then
        verify(statusHistoryRepository).save(any(StatusHistory.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
        verify(notificationService).publishNotificationEvent(any(NotificationCreatedEvent.class));
        assertNotNull(testVerificationRequest.getRejectedAt());
    }

    @Test
    void updateStatusHistory_DocumentUpdatedStatus() {
        // Given
        testStatusUpdateEvent.setToStatus(VerificationStatus.DOCUMENT_UPDATED);
        testVerificationRequest.setRequestor(testUser);
        when(verificationRequestService.getByIdOrThrow(testVerificationId)).thenReturn(testVerificationRequest);
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(statusHistoryRepository.save(any(StatusHistory.class))).thenReturn(testStatusHistory);

        // When
        statusHistoryService.updateStatusHistory(testStatusUpdateEvent);

        // Then
        verify(statusHistoryRepository).save(any(StatusHistory.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
        verify(notificationService, times(2)).publishNotificationEvent(any(NotificationCreatedEvent.class));
    }

    @Test
    void updateStatusHistory_DocumentUploadedStatus() {
        // Given
        testStatusUpdateEvent.setToStatus(VerificationStatus.DOCUMENT_UPLOADED);
        testVerificationRequest.setRequestor(testUser);
        when(verificationRequestService.getByIdOrThrow(testVerificationId)).thenReturn(testVerificationRequest);
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(statusHistoryRepository.save(any(StatusHistory.class))).thenReturn(testStatusHistory);

        // When
        statusHistoryService.updateStatusHistory(testStatusUpdateEvent);

        // Then
        verify(statusHistoryRepository).save(any(StatusHistory.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
        verify(notificationService, times(2)).publishNotificationEvent(any(NotificationCreatedEvent.class));
    }
}