package in.zeta.providers;

import in.zeta.oms.sandbox.model.object.ObjectProvider;
import in.zeta.oms.sandbox.model.realm.Realm;
import in.zeta.dto.response.AuditLogsResponse;
import in.zeta.service.AuditService;
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
public class AuditLogsProvider implements ObjectProvider<AuditLogsResponse> {
    public static final String OBJECT_TYPE = "kycsw_audit_logs";
    private final AuditService auditService;
    private static final SpectraLogger logger = OlympusSpectra.getLogger(AuditLogsProvider.class);

    @Autowired
    public AuditLogsProvider(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public CompletionStage<Optional<AuditLogsResponse>> getObject(JID jid, Realm realm, Long tenantID) {
        return CompletableFuture.supplyAsync(() -> {
            AuditLogsResponse auditLogsResponse = null;
            UUID auditLogId = null;

            try {
                auditLogId = UUID.fromString(jid.getNodeId());

                logger.info("Entry: Fetching audit log object")
                        .attr("audit log id", auditLogId)
                        .log();

                auditLogsResponse = auditService.getAuditLogById(auditLogId);

                if (auditLogsResponse == null) {
                    logger.error("audit log not found")
                            .attr("audit log id", auditLogId)
                            .log();

                    auditLogsResponse = getDefaultAuditLog();
                } else {
                    logger.info("Success: Audit log object fetched")
                            .attr("audit log id", auditLogId)
                            .log();
                }

            } catch (Exception ex) {
                logger.error("Error fetching audit log object")
                        .attr("audit log id", auditLogId)
                        .attr("error", ex.getMessage())
                        .log();

                auditLogsResponse = getDefaultAuditLog();
            }

            return Optional.of(auditLogsResponse);
        });
    }

    private AuditLogsResponse getDefaultAuditLog() {
        return new AuditLogsResponse();
    }
}
