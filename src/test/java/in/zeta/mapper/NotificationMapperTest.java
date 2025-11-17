package in.zeta.mapper;

import in.zeta.entity.Notification;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.NotificationType;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.dto.response.NotificationResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    @Test
    void toNotificationEntity_success() {
        Users customer = Users.builder().id(UUID.randomUUID()).build();
        VerificationRequest vr = VerificationRequest.builder().id(UUID.randomUUID()).customer(customer).build();
        NotificationCreatedEvent event = NotificationCreatedEvent.builder()
                .notificationId(UUID.randomUUID())
                .notificationType(NotificationType.DOCUMENT_UPLOADED)
                .message("msg")
                .build();

        Notification notification = NotificationMapper.toNotificationEntity(event, vr);

        assertNotNull(notification);
        assertEquals(event.getNotificationId(), notification.getId());
        assertEquals(customer, notification.getUser());
        assertEquals(vr, notification.getVerificationRequest());
        assertEquals(event.getNotificationType(), notification.getNotificationType());
        assertEquals(event.getMessage(), notification.getMessage());
        assertFalse(notification.getIsRead());
        assertNotNull(notification.getCreatedAt());
        assertNotNull(notification.getSentAt());
    }

    @Test
    void toNotificationResponse_success() {
        Users user = Users.builder().id(UUID.randomUUID()).build();
        VerificationRequest vr = VerificationRequest.builder().id(UUID.randomUUID()).build();
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .user(user)
                .verificationRequest(vr)
                .notificationType(NotificationType.DOCUMENT_UPLOADED)
                .message("msg")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationResponse response = NotificationMapper.toNotificationResponse(notification);

        assertNotNull(response);
        assertEquals(notification.getId(), response.getId());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(vr.getId(), response.getVerificationRequestId());
        assertEquals(notification.getNotificationType(), response.getNotificationType());
        assertEquals(notification.getMessage(), response.getMessage());
        assertEquals(notification.getIsRead(), response.isRead());
        assertEquals(notification.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void createNotificationEvent_success() {
        UUID userId = UUID.randomUUID();
        UUID vrId = UUID.randomUUID();
        NotificationCreatedEvent event = NotificationMapper.createNotificationEvent(
                userId, vrId, NotificationType.DOCUMENT_UPLOADED, "msg"
        );

        assertNotNull(event);
        assertNotNull(event.getNotificationId());
        assertEquals(userId, event.getUserId());
        assertEquals(vrId, event.getVerificationRequestId());
        assertEquals(NotificationType.DOCUMENT_UPLOADED, event.getNotificationType());
        assertEquals("msg", event.getMessage());
        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getSentAt());
    }
}