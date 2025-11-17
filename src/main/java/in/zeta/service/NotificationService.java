package in.zeta.service;

import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<NotificationResponse> getUserNotifications(UUID userId);
    List<NotificationResponse> getUnreadNotifications(UUID userId);
    Long getUnreadCount(UUID userId);
    void markAsRead(UUID notificationId, UUID userId);
    NotificationResponse getNotificationById(UUID notificationId);
    void publishNotificationEvent(NotificationCreatedEvent notificationCreatedEvent);
    void createNotificationFromEvent(NotificationCreatedEvent createNotificationRequests);
    void createNotificationFromEventPayload(String eventPayload);
}
