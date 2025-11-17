package in.zeta.service.impl;

import in.zeta.entity.Notification;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.EntityType;
import in.zeta.enums.NotificationType;
import in.zeta.enums.Role;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.dto.response.NotificationResponse;
import in.zeta.exception.InvalidOperationException;
import in.zeta.exception.ResourceNotFoundException;
import in.zeta.producer.EventProducer;
import in.zeta.repository.NotificationRepository;
import in.zeta.service.AuditService;
import in.zeta.service.UserService;
import in.zeta.service.VerificationRequestService;
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
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private VerificationRequestService verificationRequestService;

    @Mock
    private AuditService auditService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private SpectraLogger logger;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID testUserId;
    private UUID testNotificationId;
    private UUID testVerificationId;
    private Users testUser;
    private VerificationRequest testVerificationRequest;
    private Notification testNotification;
    private NotificationCreatedEvent testEvent;
    private NotificationResponse testNotificationResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNotificationId = UUID.randomUUID();
        testVerificationId = UUID.randomUUID();

        testUser = Users.builder()
                .id(testUserId)
                .email("test@example.com")
                .username("Test")
                .role(Role.CUSTOMER)
                .build();

        testVerificationRequest = VerificationRequest.builder()
                .id(testVerificationId)
                .customer(testUser)
                .build();

        testNotification = Notification.builder()
                .id(testNotificationId)
                .user(testUser)
                .verificationRequest(testVerificationRequest)
                .notificationType(NotificationType.VERIFICATION_REQUESTED)
                .message("Test notification")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        testEvent = NotificationCreatedEvent.builder()
                .notificationId(testNotificationId)
                .userId(testUserId)
                .verificationRequestId(testVerificationId)
                .notificationType(NotificationType.VERIFICATION_REQUESTED)
                .message("Test notification")
                .createdAt(LocalDateTime.now())
                .build();

        testNotificationResponse = NotificationResponse.builder()
                .id(testNotificationId)
                .message("Test notification")
                .notificationType(NotificationType.VERIFICATION_REQUESTED)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        ReflectionTestUtils.setField(notificationService, "notificationTopic", "notification-topic");
    }

    @Test
    void getUserNotifications_Success() {
        // Given
        List<NotificationResponse> notifications = Arrays.asList(testNotificationResponse);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(notifications);

        // When
        List<NotificationResponse> result = notificationService.getUserNotifications(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void getUnreadNotifications_Success() {
        // Given
        List<NotificationResponse> notifications = Arrays.asList(testNotificationResponse);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(testUserId))
                .thenReturn(notifications);

        // When
        List<NotificationResponse> result = notificationService.getUnreadNotifications(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void getUnreadCount_Success() {
        // Given
        when(notificationRepository.countByUserIdAndIsReadFalse(testUserId)).thenReturn(5L);

        // When
        Long result = notificationService.getUnreadCount(testUserId);

        // Then
        assertEquals(5L, result);
        verify(notificationRepository).countByUserIdAndIsReadFalse(testUserId);
    }

    @Test
    void markAsRead_Success() {
        // Given
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.markAsRead(testNotificationId, testUserId);

        // Then
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
        assertTrue(testNotification.getIsRead());
        assertNotNull(testNotification.getReadAt());
    }

    @Test
    void markAsRead_NotificationNotFound() {
        // Given
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markAsRead(testNotificationId, testUserId));
    }

    @Test
    void markAsRead_UnauthorizedUser() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(testNotification));

        // When & Then
        assertThrows(InvalidOperationException.class,
                () -> notificationService.markAsRead(testNotificationId, differentUserId));
    }

    @Test
    void markAsRead_AlreadyRead() {
        // Given
        testNotification.setIsRead(true);
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.markAsRead(testNotificationId, testUserId);

        // Then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(auditService, never()).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void createNotificationFromEvent_Success() {
        // Given
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createNotificationFromEvent(testEvent);

        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void createNotificationFromEvent_UserNotFound() {
        // Given
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.createNotificationFromEvent(testEvent));
    }

    @Test
    void createNotificationFromEvent_Exception() {
        // Given
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(InvalidOperationException.class,
                () -> notificationService.createNotificationFromEvent(testEvent));
    }

    @Test
    void getNotificationById_Success() {
        // Given
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(testNotification));

        // When
        NotificationResponse result = notificationService.getNotificationById(testNotificationId);

        // Then
        assertNotNull(result);
        verify(notificationRepository).findById(testNotificationId);
    }

    @Test
    void getNotificationById_NotFound() {
        // Given
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getNotificationById(testNotificationId));
    }

    @Test
    void publishNotificationEvent_Success() {
        // When
        notificationService.publishNotificationEvent(testEvent);

        // Then
        verify(eventProducer).publishEvent(
                EntityType.NOTIFICATION.toString(),
                testEvent.getNotificationId().toString(),
                "notification-topic",
                testEvent
        );
    }
}