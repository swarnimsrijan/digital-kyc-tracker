package in.zeta.service.impl;

import in.zeta.constants.Messages;
import in.zeta.entity.Document;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.EntityType;
import in.zeta.enums.NotificationType;
import in.zeta.enums.Role;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficerAssignmentServiceImplTest {

    @Mock
    private AuditService auditService;

    @Mock
    private VerificationRequestService verificationRequestService;

    @Mock
    private DocumentService documentService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OfficerAssignemetServiceImpl officerAssignmentService;

    private UUID testOfficerId;
    private UUID testVerificationId;
    private Users testOfficer;
    private Users testOfficer2;
    private VerificationRequest testVerificationRequest;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        testOfficerId = UUID.randomUUID();
        testVerificationId = UUID.randomUUID();

        testOfficer = Users.builder()
                .id(testOfficerId)
                .username("Officer1")
                .email("officer1@example.com")
                .role(Role.VERIFICATION_OFFICER)
                .build();

        testOfficer2 = Users.builder()
                .id(UUID.randomUUID())
                .username("Officer2")
                .email("officer2@example.com")
                .role(Role.VERIFICATION_OFFICER)
                .build();

        testVerificationRequest = VerificationRequest.builder()
                .id(testVerificationId)
                .status(VerificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        testDocument = Document.builder()
                .id(UUID.randomUUID())
                .fileName("test.pdf")
                .fileSize(new BigDecimal("2048000")) // 2MB
                .verificationRequest(testVerificationRequest)
                .build();
    }

    @Test
    void getOfficerWorkload_Success() {
        // Given
        List<VerificationRequest> activeRequests = Arrays.asList(testVerificationRequest);
        List<Document> documents = Arrays.asList(testDocument);

        when(userService.getByIdOrThrow(testOfficerId, "Officer not found " + testOfficerId))
                .thenReturn(testOfficer);
        when(verificationRequestService.findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.DOCUMENT_UPLOADED))
                .thenReturn(activeRequests);
        when(documentService.findByVerificationRequestId(testVerificationId))
                .thenReturn(documents);

        // When
        BigDecimal result = officerAssignmentService.getOfficerWorkload(testOfficerId);

        // Then
        assertNotNull(result);
        // Expected: requestCount(1) * 10 + totalDocuments(1) * 2 + sizeInMB(1) = 13
        assertEquals(new BigDecimal("13"), result);
        verify(userService).getByIdOrThrow(testOfficerId, "Officer not found " + testOfficerId);
        verify(verificationRequestService).findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.DOCUMENT_UPLOADED);
        verify(documentService).findByVerificationRequestId(testVerificationId);
    }

    @Test
    void getOfficerWorkload_NoActiveRequests() {
        // Given
        when(userService.getByIdOrThrow(testOfficerId, "Officer not found " + testOfficerId))
                .thenReturn(testOfficer);
        when(verificationRequestService.findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.DOCUMENT_UPLOADED))
                .thenReturn(Collections.emptyList());

        // When
        BigDecimal result = officerAssignmentService.getOfficerWorkload(testOfficerId);

        // Then
        assertEquals(BigDecimal.ZERO, result);
        verify(userService).getByIdOrThrow(testOfficerId, "Officer not found " + testOfficerId);
        verify(verificationRequestService).findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.DOCUMENT_UPLOADED);
    }

    @Test
    void getOfficerWorkload_NoDocuments() {
        // Given
        List<VerificationRequest> activeRequests = Arrays.asList(testVerificationRequest);

        when(userService.getByIdOrThrow(testOfficerId, "Officer not found " + testOfficerId))
                .thenReturn(testOfficer);
        when(verificationRequestService.findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.DOCUMENT_UPLOADED))
                .thenReturn(activeRequests);
        when(documentService.findByVerificationRequestId(testVerificationId))
                .thenReturn(Collections.emptyList());

        // When
        BigDecimal result = officerAssignmentService.getOfficerWorkload(testOfficerId);

        // Then
        // Expected: requestCount(1) * 10 + totalDocuments(0) * 2 + sizeInMB(0) = 10
        assertEquals(new BigDecimal("10"), result);
    }

    @Test
    void assignOfficerToVerification_Success() {
        // Given
        List<Users> availableOfficers = Arrays.asList(testOfficer, testOfficer2);

        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(userService.findByRole(Role.VERIFICATION_OFFICER))
                .thenReturn(availableOfficers);
        when(userService.getByIdOrThrow(testOfficerId, "Officer not found " + testOfficerId))
                .thenReturn(testOfficer);
        when(userService.getByIdOrThrow(testOfficer2.getId(), "Officer not found " + testOfficer2.getId()))
                .thenReturn(testOfficer2);
        when(verificationRequestService.findByAssignedOfficerAndStatus(any(Users.class), eq(VerificationStatus.DOCUMENT_UPLOADED)))
                .thenReturn(Collections.emptyList());

        // When
        officerAssignmentService.assignOfficerToVerification(testVerificationId);

        // Then
        assertEquals(VerificationStatus.IN_REVIEW, testVerificationRequest.getStatus());
        assertNotNull(testVerificationRequest.getAssignedOfficer());
        verify(verificationRequestService).saveVerificationRequest(testVerificationRequest);
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
        verify(notificationService).publishNotificationEvent(any(NotificationCreatedEvent.class));
    }

    @Test
    void assignOfficerToVerification_NoOfficersAvailable() {
        // Given
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(userService.findByRole(Role.VERIFICATION_OFFICER))
                .thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> officerAssignmentService.assignOfficerToVerification(testVerificationId));

        assertEquals("No verification officers available", exception.getMessage());
        verify(verificationRequestService, never()).saveVerificationRequest(any());
        verify(auditService, never()).publishAuditLogEvent(any());
        verify(notificationService, never()).publishNotificationEvent(any());
    }

    @Test
    void assignOfficerToVerification_SelectsOfficerWithLowerWorkload() {
        // Given
        List<Users> availableOfficers = Arrays.asList(testOfficer, testOfficer2);
        List<VerificationRequest> officer1Requests = Arrays.asList(testVerificationRequest);

        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(userService.findByRole(Role.VERIFICATION_OFFICER))
                .thenReturn(availableOfficers);
        when(userService.getByIdOrThrow(testOfficerId, "Officer not found " + testOfficerId))
                .thenReturn(testOfficer);
        when(userService.getByIdOrThrow(testOfficer2.getId(), "Officer not found " + testOfficer2.getId()))
                .thenReturn(testOfficer2);

        // Officer 1 has higher workload
        when(verificationRequestService.findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.DOCUMENT_UPLOADED))
                .thenReturn(officer1Requests);
        when(documentService.findByVerificationRequestId(testVerificationId))
                .thenReturn(Arrays.asList(testDocument));

        // Officer 2 has lower workload
        when(verificationRequestService.findByAssignedOfficerAndStatus(testOfficer2, VerificationStatus.DOCUMENT_UPLOADED))
                .thenReturn(Collections.emptyList());

        // When
        officerAssignmentService.assignOfficerToVerification(testVerificationId);

        // Then
        assertEquals(testOfficer2, testVerificationRequest.getAssignedOfficer());
        assertEquals(VerificationStatus.IN_REVIEW, testVerificationRequest.getStatus());
        verify(verificationRequestService).saveVerificationRequest(testVerificationRequest);
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
        verify(notificationService).publishNotificationEvent(any(NotificationCreatedEvent.class));
    }
}