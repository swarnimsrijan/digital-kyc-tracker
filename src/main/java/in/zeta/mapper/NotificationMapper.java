package in.zeta.mapper;
import in.zeta.entity.Notification;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.NotificationType;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.dto.response.NotificationResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationMapper {
    public static Notification toNotificationEntity(NotificationCreatedEvent request, VerificationRequest verificationRequest) {
        return Notification.builder()
                .id(request.getNotificationId())
                .user(verificationRequest.getCustomer())
                .verificationRequest(verificationRequest)
                .notificationType(request.getNotificationType())
                .message(request.getMessage())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .verificationRequestId(notification.getVerificationRequest().getId())
                .notificationType(notification.getNotificationType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public static NotificationCreatedEvent createNotificationEvent(UUID receiverId, UUID verificationRequestId, NotificationType notificationType, String message) {
        return NotificationCreatedEvent.builder()
                .notificationId(UUID.randomUUID())
                .userId(receiverId)
                .verificationRequestId(verificationRequestId)
                .notificationType(notificationType)
                .message(message)
                .createdAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();
    }
    private NotificationMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
}
