package in.zeta.service.impl;

import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.Role;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.requests.CreateVerificationRequest;
import in.zeta.dto.requests.UpdateVerificationStatusRequest;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.dto.requests.events.StatusUpdateEvent;
import in.zeta.dto.response.CreateVerificationResponse;
import in.zeta.dto.response.VerificationRequestResponse;
import in.zeta.exception.ResourceNotFoundException;
import in.zeta.repository.VerificationRequestRepository;
import in.zeta.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationRequestServiceImplTest {

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @Mock
    private VerificationRequestLimitService verificationRequestLimitService;

    @Mock
    private UserService userService;

    @Mock
    private StatusHistoryService statusHistoryService;

    @InjectMocks
    private VerificationRequestServiceImpl verificationRequestService;

    private UUID testVerificationId;
    private UUID testRequestorId;
    private UUID testCustomerId;
    private UUID testOfficerId;
    private Users testRequestor;
    private Users testCustomer;
    private Users testOfficer;
    private VerificationRequest testVerificationRequest;
    private CreateVerificationRequest testCreateRequest;

    @BeforeEach
    void setUp() {
        testVerificationId = UUID.randomUUID();
        testRequestorId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        testOfficerId = UUID.randomUUID();

        testRequestor = Users.builder()
                .id(testRequestorId)
                .email("requestor@example.com")
                .username("Requestor")
                .role(Role.VERIFICATION_REQUESTOR)
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
                .requestor(testRequestor)
                .status(VerificationStatus.PENDING)
                .requestReason("Test reason")
                .createdAt(LocalDateTime.now())
                .build();

        testCreateRequest = CreateVerificationRequest.builder()
                .customerId(testCustomerId)
                .requestReason("Test verification request")
                .build();
    }

    @Test
    void createVerificationRequest_RequestorNotFound() {
        // Given
        when(userService.getByIdOrThrow(testRequestorId, "Requestor not found " + testRequestorId))
                .thenThrow(new ResourceNotFoundException("Requestor not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> verificationRequestService.createVerificationRequest(testCreateRequest, testRequestorId));
    }

    @Test
    void createVerificationRequest_CustomerNotFound() {
        // Given
        when(userService.getByIdOrThrow(testRequestorId, "Requestor not found " + testRequestorId))
                .thenReturn(testRequestor);
        when(userService.getByIdOrThrow(testCustomerId, "Customer not found " + testCustomerId))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> verificationRequestService.createVerificationRequest(testCreateRequest, testRequestorId));
    }


    @Test
    void getVerificationRequest_Success() {
        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.of(testVerificationRequest));

        VerificationRequestResponse result = verificationRequestService.getVerificationRequest(testVerificationId);

        assertNotNull(result);
        verify(verificationRequestRepository).findById(testVerificationId);
    }

    @Test
    void getVerificationRequest_NotFound() {
        // Given
        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> verificationRequestService.getVerificationRequest(testVerificationId));
    }

    @Test
    void getCustomerVerifications_Success() {
        // Given
        List<VerificationRequest> requests = Arrays.asList(testVerificationRequest);
        when(verificationRequestRepository.findByCustomerId(testCustomerId)).thenReturn(requests);

        // When
        List<VerificationRequestResponse> result = verificationRequestService.getCustomerVerifications(testCustomerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(verificationRequestRepository).findByCustomerId(testCustomerId);
    }

    @Test
    void getOfficerVerifications_Success() {
        // Given
        List<VerificationRequest> requests = Arrays.asList(testVerificationRequest);
        when(verificationRequestRepository.findByAssignedOfficerId(testOfficerId)).thenReturn(requests);

        // When
        List<VerificationRequestResponse> result = verificationRequestService.getOfficerVerifications(testOfficerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(verificationRequestRepository).findByAssignedOfficerId(testOfficerId);
    }

    @Test
    void getOpenVerifications_Success() {
        // Given
        List<VerificationRequest> requests = Arrays.asList(testVerificationRequest);
        when(verificationRequestRepository.findByStatus(VerificationStatus.PENDING)).thenReturn(requests);

        // When
        List<VerificationRequestResponse> result = verificationRequestService.getOpenVerifications();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(verificationRequestRepository).findByStatus(VerificationStatus.PENDING);
    }

    @Test
    void updateStatus_Success() {
        // Given
        testVerificationRequest.setAssignedOfficer(testOfficer);
        UpdateVerificationStatusRequest updateRequest = UpdateVerificationStatusRequest.builder()
                .status(VerificationStatus.APPROVED)
                .reason("Approved after review")
                .build();

        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.of(testVerificationRequest));
        when(verificationRequestRepository.save(any(VerificationRequest.class)))
                .thenReturn(testVerificationRequest);

        // When
        VerificationRequestResponse result = verificationRequestService.updateStatus(
                testVerificationId, updateRequest, testOfficerId);

        // Then
        assertNotNull(result);
        verify(verificationRequestRepository).save(any(VerificationRequest.class));
        verify(statusHistoryService).publishStatusUpdateEvent(any(StatusUpdateEvent.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void updateStatus_NoOfficerAssigned() {
        // Given
        UpdateVerificationStatusRequest updateRequest = UpdateVerificationStatusRequest.builder()
                .status(VerificationStatus.APPROVED)
                .reason("Approved after review")
                .build();

        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.of(testVerificationRequest));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> verificationRequestService.updateStatus(testVerificationId, updateRequest, testOfficerId));
    }

    @Test
    void updateStatus_WrongOfficer() {
        // Given
        testVerificationRequest.setAssignedOfficer(testOfficer);
        UUID differentOfficerId = UUID.randomUUID();
        UpdateVerificationStatusRequest updateRequest = UpdateVerificationStatusRequest.builder()
                .status(VerificationStatus.APPROVED)
                .reason("Approved after review")
                .build();

        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.of(testVerificationRequest));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> verificationRequestService.updateStatus(testVerificationId, updateRequest, differentOfficerId));
    }

    @Test
    void assignToOfficer_Success() {
        // Given
        Users currentOfficer = Users.builder()
                .id(UUID.randomUUID())
                .email("current@example.com")
                .username("CurrentOfficer")
                .role(Role.VERIFICATION_OFFICER)
                .build();

        testVerificationRequest.setAssignedOfficer(currentOfficer); // Set current officer

        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.of(testVerificationRequest));
        when(userService.getByIdOrThrow(testOfficerId, "Officer not found with ID: " + testOfficerId))
                .thenReturn(testOfficer);
        when(verificationRequestRepository.save(any(VerificationRequest.class)))
                .thenReturn(testVerificationRequest);

        // When
        VerificationRequestResponse result = verificationRequestService.assignToOfficer(
                testVerificationId, testOfficerId);

        // Then
        assertNotNull(result);
        verify(verificationRequestRepository).save(any(VerificationRequest.class));
        verify(notificationService).publishNotificationEvent(any(NotificationCreatedEvent.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void getPendingRequestsAssignedToOfficer_Success() {
        // Given
        testVerificationRequest.setAssignedOfficer(testOfficer);
        List<VerificationRequest> requests = Arrays.asList(testVerificationRequest);
        when(verificationRequestRepository.findByAssignedOfficerId(testOfficerId)).thenReturn(requests);

        // When
        List<VerificationRequestResponse> result = verificationRequestService
                .getPendingRequestsAssignedToOfficer(testOfficerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(verificationRequestRepository).findByAssignedOfficerId(testOfficerId);
    }

    @Test
    void getVerificationStatus_Success() {
        // Given
        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.of(testVerificationRequest));

        // When
        VerificationStatus result = verificationRequestService.getVerificationStatus(testVerificationId);

        // Then
        assertEquals(VerificationStatus.PENDING, result);
        verify(verificationRequestRepository).findById(testVerificationId);
    }

    @Test
    void getUserByVerificationRequest_Success() {
        // Given
        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.of(testVerificationRequest));

        // When
        Users result = verificationRequestService.getUserByVerificationRequest(testVerificationId);

        // Then
        assertEquals(testCustomer, result);
        verify(verificationRequestRepository).findById(testVerificationId);
    }

    @Test
    void getByIdOrThrow_Success() {
        // Given
        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.of(testVerificationRequest));

        // When
        VerificationRequest result = verificationRequestService.getByIdOrThrow(testVerificationId);

        // Then
        assertEquals(testVerificationRequest, result);
        verify(verificationRequestRepository).findById(testVerificationId);
    }

    @Test
    void getByIdOrThrow_NotFound() {
        // Given
        when(verificationRequestRepository.findById(testVerificationId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> verificationRequestService.getByIdOrThrow(testVerificationId));
    }

    @Test
    void saveVerificationRequest_Success() {
        // When
        verificationRequestService.saveVerificationRequest(testVerificationRequest);

        // Then
        verify(verificationRequestRepository).save(testVerificationRequest);
    }

    @Test
    void findByAssignedOfficerAndStatus_Success() {
        // Given
        List<VerificationRequest> requests = Arrays.asList(testVerificationRequest);
        when(verificationRequestRepository.findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.PENDING))
                .thenReturn(requests);

        // When
        List<VerificationRequest> result = verificationRequestService
                .findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.PENDING);

        // Then
        assertEquals(requests, result);
        verify(verificationRequestRepository).findByAssignedOfficerAndStatus(testOfficer, VerificationStatus.PENDING);
    }

    @Test
    void save_Success() {
        // When
        verificationRequestService.save(testVerificationRequest);

        // Then
        verify(verificationRequestRepository).save(testVerificationRequest);
    }

    @Test
    void createVerificationRequest_Success() {
        // Given
        when(userService.getByIdOrThrow(testRequestorId, "Requestor not found " + testRequestorId))
                .thenReturn(testRequestor);
        when(userService.getByIdOrThrow(testCustomerId, "Customer not found " + testCustomerId))
                .thenReturn(testCustomer);
        when(verificationRequestLimitService.canCreateVerificationRequest(testCustomer, testRequestor))
                .thenReturn(true);
        when(verificationRequestRepository.save(any(VerificationRequest.class)))
                .thenReturn(testVerificationRequest);

        // When
        CreateVerificationResponse result = verificationRequestService.createVerificationRequest(
                testCreateRequest, testRequestorId);

        // Then
        assertNotNull(result);
        verify(verificationRequestRepository).save(any(VerificationRequest.class));
        verify(verificationRequestLimitService).incrementRequestCount(testCustomer, testRequestor);
        verify(statusHistoryService).publishStatusUpdateEvent(any(StatusUpdateEvent.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void getRequestorVerifications_Success() {
        // Given
        when(userService.getByIdOrThrow(testRequestorId, "Requestor not found with ID: " + testRequestorId))
                .thenReturn(testRequestor);
        List<VerificationRequest> requests = Arrays.asList(testVerificationRequest);
        when(verificationRequestRepository.findByRequestorId(testRequestorId)).thenReturn(requests);

        // When
        List<VerificationRequestResponse> result = verificationRequestService.getRequestorVerifications(testRequestorId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService).getByIdOrThrow(testRequestorId, "Requestor not found with ID: " + testRequestorId);
        verify(verificationRequestRepository).findByRequestorId(testRequestorId);
    }
}