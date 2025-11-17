package in.zeta.providers;

import in.zeta.oms.sandbox.model.realm.Realm;
import in.zeta.dto.response.CommentResponse;
import in.zeta.service.CommentService;
import in.zeta.spectra.capture.SpectraLogger;
import olympus.common.JID;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import in.zeta.oms.sandbox.model.object.ObjectProvider;

@Component
public class CommentProvider implements ObjectProvider<CommentResponse>{
    public static final String OBJECT_TYPE = "kycsw_comment";
    private final CommentService commentService;

    private static final SpectraLogger logger = OlympusSpectra.getLogger(CommentProvider.class);
    @Autowired
    public CommentProvider(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public CompletionStage<Optional<CommentResponse>> getObject(JID jid, Realm realm, Long tenantID) {
        return CompletableFuture.supplyAsync(() -> {
            CommentResponse commentResponse = null;
            UUID commentId = null;

            try {
                commentId = UUID.fromString(jid.getNodeId());

                logger.info("Entry: Fetching comment object")
                        .attr("status history id", commentId)
                        .log();

                commentResponse = commentService.getCommentById(commentId);

                if (commentResponse == null) {

                    logger.error("status history not found")
                            .attr("status history id", commentId)
                            .log();

                    commentResponse = getDefaultStatusHistory();

                } else {

                    logger.info("Success: User object fetched")
                            .attr("status history", commentId)
                            .log();

                }

            } catch (Exception ex) {

                logger.error("Error fetching status  history object")
                        .attr("status history", commentId)
                        .attr("error", ex.getMessage())
                        .log();

                commentResponse = getDefaultStatusHistory();
            }

            return Optional.of(commentResponse);
        });
    }

    private CommentResponse getDefaultStatusHistory() {
        return new CommentResponse();
    }
}
