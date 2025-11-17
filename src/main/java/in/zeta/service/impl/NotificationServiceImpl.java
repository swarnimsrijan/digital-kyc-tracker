package in.zeta.service.impl;

import in.zeta.entity.Notification;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.exception.InvalidOperationException;
import in.zeta.exception.ResourceNotFoundException;
import in.zeta.producer.EventProducer;
import in.zeta.repository.NotificationRepository;
import in.zeta.dto.response.NotificationResponse;
import in.zeta.service.AuditService;
import in.zeta.service.NotificationService;
import in.zeta.service.UserService;
import in.zeta.service.VerificationRequestService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static in.zeta.mapper.AuditLogMapper.createAuditLogEvent;
import static in.zeta.mapper.NotificationMapper.toNotificationEntity;
import static in.zeta.mapper.NotificationMapper.toNotificationResponse;


@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Value("${atropos.notification.topic}")
    private String notificationTopic;

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final VerificationRequestService verificationRequestService;
    private final AuditService auditService;
    private static final SpectraLogger logger = OlympusSpectra.getLogger(NotificationServiceImpl.class);
    private final EventProducer eventProducer;

    @Override
    public List<NotificationResponse> getUserNotifications(UUID userId) {

        logger.info("Fetching notifications for userId:")
                .attr("userId", userId)
                .log();

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {

        logger.info("Fetching unread notifications for userId:")
                .attr("userId", userId)
                .log();

        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public Long getUnreadCount(UUID userId) {

        logger.info("Counting unread notifications for userId:")
                .attr("userId", userId)
                .log();

        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("User not authorized to mark this notification as read");
        }

        logger.info("Marking notification as read:")
                .attr("notificationId", notificationId)
                .attr("status", notification.getIsRead())
                .log();

        if (!notification.getIsRead()) {
            String oldValue = "unread";
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);

            AuditLogCreatedEvent event = createAuditLogEvent(
                    EntityType.NOTIFICATION,
                    notificationId,
                    AuditAction.NOTIFICATION_READ,
                    notification.getUser(),
                    oldValue,
                    String.format("Notification read successfully: %s", notification.getId())
            );

            auditService.publishAuditLogEvent(event);
        }

        logger.info("Notification marked as read:")
                .attr("notificationId", notificationId)
                .attr("status", notification.getIsRead())
                .attr("readAt", notification.getReadAt())
                .log();

        notificationRepository.save(notification);
    }

    @Override
    public void createNotificationFromEvent(NotificationCreatedEvent notificationCreatedEvent) {
        Users user = userService.getByIdOrThrow(notificationCreatedEvent.getUserId(), "User not found with ID: " + notificationCreatedEvent.getUserId());
        VerificationRequest verificationRequest = verificationRequestService.getByIdOrThrow(notificationCreatedEvent.getVerificationRequestId());

        logger.info("Creating notification:")
                .attr("userId", user.getId())
                .attr("verificationRequestId", verificationRequest.getId())
                .attr("notificationType", notificationCreatedEvent.getNotificationType())
                .log();

        try {
            Notification notification = toNotificationEntity(notificationCreatedEvent, verificationRequest);

            logger.info("Notification created:")
                    .attr("userId", user.getId())
                    .attr("notificationType", notification.getNotificationType())
                    .attr("createdAt", notification.getCreatedAt())
                    .log();

            Notification savedNotification = notificationRepository.save(notification);

            // here i want you to write logic for sending email to the recepient using email service later on implementation

            AuditLogCreatedEvent event = createAuditLogEvent(
                    EntityType.NOTIFICATION,
                    savedNotification.getId(),
                    AuditAction.NOTIFICATION_SENT,
                    notification.getUser(),
                    "",
                    String.format("Notification created successfully: %s", notification.getId())
            );

            auditService.publishAuditLogEvent(event);

            logger.info("Notification created and audit logged")
                    .attr("notificationId", savedNotification.getId())
                    .attr("recipient", user.getId())
                    .attr("type", notificationCreatedEvent.getNotificationType())
                    .log();

        } catch (Exception e) {

            logger.error("Failed to create notification:")
                    .attr("userId", notificationCreatedEvent.getUserId())
                    .attr("verificationRequestId", notificationCreatedEvent.getVerificationRequestId())
                    .attr("notificationType", notificationCreatedEvent.getNotificationType())
                    .attr("error", e.getMessage())
                    .log();

            throw new InvalidOperationException("Failed to create notification: " + e.getMessage());
        }
    }

    @Override
    public NotificationResponse getNotificationById(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        return toNotificationResponse(notification);
    }

    @Override
    public void publishNotificationEvent(NotificationCreatedEvent notificationCreatedEvent) {
        logger.info("Creating notification event")
                .attr("for userId", notificationCreatedEvent.getUserId())
                .attr("notificationType", notificationCreatedEvent.getNotificationType())
                .attr("verificationRequestId", notificationCreatedEvent.getVerificationRequestId())
                .attr("createdAt", notificationCreatedEvent.getCreatedAt())
                .log();

        eventProducer.publishEvent(
                EntityType.NOTIFICATION.toString(),
                notificationCreatedEvent.getNotificationId().toString(),
                notificationTopic,
                notificationCreatedEvent);

        logger.info("Published notification created event")
                .attr("notificationId", notificationCreatedEvent.getNotificationId())
                .attr("for userId", notificationCreatedEvent.getUserId())
                .attr("notificationType", notificationCreatedEvent.getNotificationType())
                .attr("sentAt", notificationCreatedEvent.getSentAt())
                .log();
    }

    @Override
    public void createNotificationFromEventPayload(String eventPayload) {
        try {
            logger.info("Processing notification event from payload")
                    .attr("eventPayload", eventPayload)
                    .log();

            NotificationCreatedEvent notificationCreatedEvent = JsonUtil.parseNotificationCreatedEvent(eventPayload);

            createNotificationFromEvent(notificationCreatedEvent);

            logger.info("Successfully processed notification event from payload")
                    .attr("notificationId", notificationCreatedEvent.getNotificationId())
                    .attr("userId", notificationCreatedEvent.getUserId())
                    .attr("notificationType", notificationCreatedEvent.getNotificationType())
                    .log();

        } catch (Exception e) {
            logger.error("Failed to process notification event from payload")
                    .attr("eventPayload", eventPayload)
                    .attr("error", e.getMessage())
                    .log();
            throw new RuntimeException("Failed to process notification event from payload", e);
        }
    }
}