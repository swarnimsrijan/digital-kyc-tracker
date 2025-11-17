package in.zeta.providers;

import in.zeta.oms.sandbox.model.object.ObjectProvider;
import in.zeta.oms.sandbox.model.realm.Realm;
import in.zeta.dto.response.VerificationRequestResponse;
import in.zeta.service.VerificationRequestService;
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
public class VerificationRequestsProvider implements ObjectProvider<VerificationRequestResponse> {
    public static final String OBJECT_TYPE = "kycsw_verification_request";
    private final VerificationRequestService verificationRequestService;
    private static final SpectraLogger logger = OlympusSpectra.getLogger(VerificationRequestsProvider.class);

    @Autowired
    public VerificationRequestsProvider(VerificationRequestService verificationRequestService) {
        this.verificationRequestService = verificationRequestService;
    }

    @Override
    public CompletionStage<Optional<VerificationRequestResponse>> getObject(JID jid, Realm realm, Long tenantID) {
        return CompletableFuture.supplyAsync(() -> {
            VerificationRequestResponse verificationRequestResponse = null;
            UUID verificationRequestId = null;

            try {
                verificationRequestId = UUID.fromString(jid.getNodeId());

                logger.info("Entry: Fetching verification request object")
                        .attr("verification request id", verificationRequestId)
                        .log();

                verificationRequestResponse = verificationRequestService.getVerificationRequest(verificationRequestId);

                if (verificationRequestResponse == null) {
                    logger.error("verification request not found")
                            .attr("verification request id", verificationRequestId)
                            .log();

                    verificationRequestResponse = getDefaultVerificationRequest();
                } else {
                    logger.info("Success: Verification request object fetched")
                            .attr("verification request id", verificationRequestId)
                            .log();
                }

            } catch (Exception ex) {
                logger.error("Error fetching verification request object")
                        .attr("verification request id", verificationRequestId)
                        .attr("error", ex.getMessage())
                        .log();

                verificationRequestResponse = getDefaultVerificationRequest();
            }

            return Optional.of(verificationRequestResponse);
        });
    }

    private VerificationRequestResponse getDefaultVerificationRequest() {
        return new VerificationRequestResponse();
    }
}