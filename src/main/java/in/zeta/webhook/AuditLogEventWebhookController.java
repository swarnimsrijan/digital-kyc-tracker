package in.zeta.webhook;

import in.zeta.dto.response.ApiResponse;
import in.zeta.service.AuditService;
import in.zeta.spectra.capture.SpectraLogger;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogEventWebhookController {

    private final SpectraLogger logger = OlympusSpectra.getLogger(AuditLogEventWebhookController.class);
    private final AuditService auditService;

    @PostMapping("/events/webhook")
    public ResponseEntity<ApiResponse<String>> consumeAuditEvent(@RequestBody String eventPayload) {

        try {
            logger.info("Consuming audit log event")
                    .attr("event", eventPayload)
                    .log();

            auditService.processAuditLogEvent(eventPayload);

            logger.info("Audit log created successfully for event").log();

            return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message("Event consumed").build());
        } catch (Exception e) {
            logger.error("Failed to process audit log event")
                    .attr("error", e.getMessage())
                    .log();
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder().success(false).message("Failed to process event: " + e.getMessage()).build());
        }
    }
}
