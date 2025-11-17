package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.providers.NotificationProvider;
import in.zeta.dto.response.ApiResponse;
import in.zeta.dto.response.NotificationResponse;
import in.zeta.service.NotificationService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAuthorizedSync;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import static in.zeta.constants.Messages.Keys.NOTIFICATION_COUNT;
import static in.zeta.constants.Messages.Keys.NOTIFICATION_ID;
import static in.zeta.constants.Messages.Notification.*;

@RestController
@RequestMapping("/tenants/{tenantId}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    private final static SpectraLogger logger = OlympusSpectra.getLogger(NotificationController.class);

    @GetMapping("/user/{userId}")
    @SandboxAuthorizedSync(action = "notification.read", object = "$$userId$$@" + NotificationProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @PathVariable UUID userId) {

        logger.info(Messages.Notification.FETCHING_NOTIFICATIONS_FOR_USER)
                    .attr(Messages.Keys.USER_ID, userId)
                    .log();

        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);

        logger.info(FETCHED_NOTIFICATIONS_FOR_USER)
                        .attr(Messages.Keys.USER_ID, userId)
                        .attr(NOTIFICATION_COUNT, notifications.size())
                        .log();

        return ResponseEntity.ok(ApiResponse.success(FETCHED_NOTIFICATIONS_FOR_USER, notifications));
    }

    @GetMapping("/unread/user/{userId}")
    @SandboxAuthorizedSync(action = "notification.read", object = "$$userId$$@" + NotificationProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @PathVariable UUID userId) {

        logger.info(FETCHING_UNREAD_COUNT_FOR_USER)
                    .attr(Messages.Keys.USER_ID, userId)
                    .log();

        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);

        logger.info(UNREAD_COUNT_FOR_USER_FETCHED)
                        .attr(Messages.Keys.USER_ID, userId)
                        .attr(NOTIFICATION_COUNT, notifications.size())
                        .log();

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread/count/user/{userId}")
    @SandboxAuthorizedSync(action = "notification.read", object = "$$userId$$@" + NotificationProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @PathVariable UUID userId) {

        logger.info(FETCHING_UNREAD_COUNT_FOR_USER)
                    .attr(Messages.Keys.USER_ID, userId)
                    .log();

        Long count = notificationService.getUnreadCount(userId);

        logger.info(UNREAD_COUNT_FOR_USER_FETCHED)
                        .attr(Messages.Keys.USER_ID, userId)
                        .attr(NOTIFICATION_COUNT, count)
                        .log();

        return ResponseEntity.ok(ApiResponse.success(NOTIFICATION_COUNT,count));
    }

    @PutMapping("/{notificationId}/user/{userId}/read")
    @SandboxAuthorizedSync(action = "notification.read", object = "$$userId$$@" + NotificationProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID notificationId,
            @PathVariable UUID userId) {

        logger.info(MARKING_AS_READ)
                    .attr(NOTIFICATION_ID, notificationId)
                    .log();

        notificationService.markAsRead(notificationId, userId);

        logger.info(MARKED_AS_READ_SUCCESSFULLY)
                    .attr(NOTIFICATION_ID, notificationId)
                    .log();

        return ResponseEntity.ok(ApiResponse.success(MARKED_AS_READ_SUCCESSFULLY, null));
    }

}