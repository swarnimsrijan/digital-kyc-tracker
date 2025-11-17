package in.zeta.providers;

import in.zeta.oms.sandbox.model.object.ObjectProvider;
import in.zeta.oms.sandbox.model.realm.Realm;
import in.zeta.dto.response.NotificationResponse;
import in.zeta.service.NotificationService;
import in.zeta.spectra.capture.SpectraLogger;
import olympus.common.JID;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class NotificationProvider implements ObjectProvider<NotificationResponse> {
    public static final String OBJECT_TYPE = "kycsw_notification";
    private final NotificationService notificationService;
    private static final SpectraLogger logger = OlympusSpectra.getLogger(NotificationProvider.class);

    @Autowired
    public NotificationProvider(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public CompletionStage<Optional<NotificationResponse>> getObject(JID jid, Realm realm, Long tenantID) {
        return CompletableFuture.supplyAsync(() -> {
            NotificationResponse notificationResponse = null;
            UUID notificationId = null;

            try {
                notificationId = UUID.fromString(jid.getNodeId());

                logger.info("Entry: Fetching notification object")
                        .attr("notification id", notificationId)
                        .log();

                notificationResponse = notificationService.getNotificationById(notificationId);

                if (notificationResponse == null) {
                    logger.error("notification not found")
                            .attr("notification id", notificationId)
                            .log();

                    notificationResponse = getDefaultNotification();
                } else {
                    logger.info("Success: Notification object fetched")
                            .attr("notification id", notificationId)
                            .log();
                }

            } catch (Exception ex) {
                logger.error("Error fetching notification object")
                        .attr("notification id", notificationId)
                        .attr("error", ex.getMessage())
                        .log();

                notificationResponse = getDefaultNotification();
            }

            return Optional.of(notificationResponse);
        });
    }

    private NotificationResponse getDefaultNotification() {
        return new NotificationResponse();
    }
}