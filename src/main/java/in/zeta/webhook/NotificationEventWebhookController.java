package in.zeta.webhook;

import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.dto.response.ApiResponse;
import in.zeta.service.NotificationService;
import in.zeta.spectra.capture.SpectraLogger;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationEventWebhookController {

    private final NotificationService notificationService;
    private final SpectraLogger logger = OlympusSpectra.getLogger(NotificationEventWebhookController.class);

    @PostMapping("/events/webhook")
    public ResponseEntity<ApiResponse<String>> consumeNotificationEvent(@RequestBody String eventPayload) {

        try {
            logger.info("Consuming notification event")
                    .attr("event", eventPayload)
                    .log();

            notificationService.createNotificationFromEventPayload(eventPayload);

            logger.info("Notification created successfully for event")
                    .log();

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Event consumed")
                    .build());
        } catch (Exception e) {
            logger.error("Failed to process notification event")
                    .attr("error", e.getMessage())
                    .log();
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Failed to process event: " + e.getMessage())
                    .build());
        }
    }
}
