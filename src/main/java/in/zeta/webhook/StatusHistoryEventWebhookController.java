package in.zeta.webhook;

import in.zeta.dto.requests.events.StatusUpdateEvent;
import in.zeta.dto.response.ApiResponse;
import in.zeta.service.StatusHistoryService;
import in.zeta.spectra.capture.SpectraLogger;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
public class StatusHistoryEventWebhookController {
    private final SpectraLogger logger = OlympusSpectra.getLogger(StatusHistoryEventWebhookController.class);
    private final StatusHistoryService statusHistoryService;

    @PostMapping("/events/webhook")
    public ResponseEntity<ApiResponse<String>> consumeStatusUpdateEvent(@RequestBody String eventPayload) {

        try{
            logger.info("Consuming status update event")
                    .attr("event", eventPayload)
                    .log();

            statusHistoryService.updateStatusHistoryFromEvent(eventPayload);

            logger.info("Status history updated successfully for event")
                    .log();

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Event consumed")
                    .build());

        } catch (Exception e) {
            logger.error("Failed to parse event payload", e)
                    .attr("eventPayload", eventPayload)
                    .log();
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Invalid event payload")
                    .build());
        }
    }
}