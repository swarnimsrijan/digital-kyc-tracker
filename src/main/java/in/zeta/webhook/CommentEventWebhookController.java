package in.zeta.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.dto.response.ApiResponse;
import in.zeta.enums.AuditAction;
import in.zeta.dto.requests.events.CommentCreatedEvent;
import in.zeta.dto.requests.events.CommentDeletedEvent;
import in.zeta.dto.requests.events.CommentUpdatedEvent;
import in.zeta.service.AuditService;
import in.zeta.service.CommentService;
import in.zeta.service.UserService;
import in.zeta.spectra.capture.SpectraLogger;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentEventWebhookController {
    private final SpectraLogger logger = OlympusSpectra.getLogger(CommentEventWebhookController.class);
    private CommentService commentService;

    @Autowired
    public CommentEventWebhookController(CommentService commentService) {
        this.commentService = commentService;

    }

    @PostMapping("/events/webhook")
    public ResponseEntity<ApiResponse> handleCommentEvents(@RequestBody String eventPayload) {

        try{
            logger.info("Received comment event")
                    .log();

            commentService.processCommentEvent(eventPayload);

            logger.info("Comment event processed successfully")
                    .log();

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Event consumed")
                    .build());

        } catch (Exception e) {
            logger.error("Error processing comment event")
                    .attr("error", e.getMessage())
                    .log();

            return ResponseEntity.status(500).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Error processing event: " + e.getMessage())
                    .build());
        }
    }
}
