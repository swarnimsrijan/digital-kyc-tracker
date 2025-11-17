package in.zeta.controller;

import in.zeta.enums.VerificationStatus;
import in.zeta.providers.StatusHistoryProvider;
import in.zeta.dto.response.ApiResponse;
import in.zeta.dto.response.StatusHistoryResponse;
import in.zeta.service.StatusHistoryService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAuthorizedSync;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import static in.zeta.constants.Messages.Keys.*;
import static in.zeta.constants.Messages.Status.*;
import static in.zeta.constants.Messages.Verification.VERIFICATION_STATUS;

@RestController
@RequestMapping("/tenants/{tenantId}/verification")
@RequiredArgsConstructor
public class StatusHistoryController {

    private static final SpectraLogger logger = OlympusSpectra.getLogger(StatusHistoryController.class);
    private final StatusHistoryService statusHistoryService;

    @GetMapping("/{verificationId}/status")
    @SandboxAuthorizedSync(action = "status.read", object = "$$verificationId$$@" + StatusHistoryProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<VerificationStatus>> getVerificationStatus(
            @PathVariable UUID verificationId) {

        logger.info(FETCHING_VERIFICATION_STATUS)
                .attr(VERIFICATION_ID, verificationId)
                .log();

        VerificationStatus status = statusHistoryService
                .getLatestStatus(verificationId);

        logger.info(FETCHED_VERIFICATION_STATUS)
                .attr(VERIFICATION_ID, verificationId)
                .attr(STATUS, status)
                .log();

        return ResponseEntity.ok(ApiResponse.success(VERIFICATION_STATUS, status));
    }

    @GetMapping("/{verificationId}/status/history")
    @SandboxAuthorizedSync(action = "status.read", object = "$$verificationId$$@" + StatusHistoryProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<StatusHistoryResponse>>> getStatusHistory(
            @PathVariable UUID verificationId) {

        logger.info(FETCHING_STATUS_HISTORY)
                .attr(VERIFICATION_ID, verificationId)
                .log();

        List<StatusHistoryResponse> history = statusHistoryService.getStatusHistoryByVerificationId(verificationId);

        logger.info(FETCHED_STATUS_HISTORY)
                .attr(VERIFICATION_ID, verificationId)
                .attr(HISTORY_COUNT, history.size())
                .log();

        return ResponseEntity.ok(ApiResponse.success(FETCHED_STATUS_HISTORY, history));
    }
}
