package in.zeta.providers;

import in.zeta.oms.sandbox.model.object.ObjectProvider;
import in.zeta.oms.sandbox.model.realm.Realm;
import in.zeta.dto.response.StatusHistoryResponse;
import in.zeta.service.StatusHistoryService;
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
public class StatusHistoryProvider implements ObjectProvider<StatusHistoryResponse> {
    public static final String OBJECT_TYPE = "kycsw_status";
    private final StatusHistoryService statusHistoryService;
    private static final SpectraLogger logger = OlympusSpectra.getLogger(StatusHistoryProvider.class);
    @Autowired
    public StatusHistoryProvider(StatusHistoryService statusHistoryService) {
        this.statusHistoryService = statusHistoryService;
    }

    @Override
    public CompletionStage<Optional<StatusHistoryResponse>> getObject(JID jid, Realm realm, Long tenantID) {
        return CompletableFuture.supplyAsync(() -> {
            StatusHistoryResponse statusHistoryResponse = null;
            UUID statusHistoryid = null;

            try {
                statusHistoryid = UUID.fromString(jid.getNodeId());

                logger.info("Entry: Fetching status history object")
                        .attr("status history id", statusHistoryid)
                        .log();

                statusHistoryResponse = statusHistoryService.getStatusHistoryById(statusHistoryid);

                if (statusHistoryResponse == null) {

                    logger.error("status history not found")
                            .attr("status history id", statusHistoryid)
                            .log();

                    statusHistoryResponse = getDefaultStatusHistory();

                } else {

                    logger.info("Success: User object fetched")
                            .attr("status history", statusHistoryid)
                            .log();

                }

            } catch (Exception ex) {

                logger.error("Error fetching status  history object")
                        .attr("status history", statusHistoryid)
                        .attr("error", ex.getMessage())
                        .log();

                statusHistoryResponse = getDefaultStatusHistory();
            }

            return Optional.of(statusHistoryResponse);
        });
    }

    private StatusHistoryResponse getDefaultStatusHistory() {
        return new StatusHistoryResponse();
    }
}
