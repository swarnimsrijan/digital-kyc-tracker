package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.enums.EntityType;
import in.zeta.enums.AuditAction;
import in.zeta.providers.AuditLogsProvider;
import in.zeta.dto.response.ApiResponse;
import in.zeta.dto.response.AuditLogsResponse;
import in.zeta.service.AuditService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAuthorizedSync;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import static in.zeta.constants.Messages.Audit.*;
import static in.zeta.constants.Messages.Keys.*;

@RestController
@RequestMapping("tenants/{tenantId}/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    private static final SpectraLogger logger = OlympusSpectra.getLogger(AuditController.class);

    @GetMapping("/entityType/{entityType}/entityId/{entityId}")
    @SandboxAuthorizedSync(action = "audit.read", object = "$$entityId$$@" + AuditLogsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<AuditLogsResponse>>> getAuditTrail(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") UUID entityId) {

        logger.info(FETCHING_AUDIT_TRAIL)
                .attr(ENTITY_TYPE, entityType)
                .attr(Messages.Keys.ENTITY_ID, entityId)
                .log();

        List<AuditLogsResponse> auditLogs = auditService.getAuditTrail(entityType, entityId);

        logger.info(Messages.Audit.FETCHED_TRAIL_SUCCESSFULLY)
                .attr(ENTITY_TYPE, entityType)
                .attr(ENTITY_ID, entityId)
                .attr(Messages.Keys.LOG_COUNT, auditLogs.size())
                .log();

        return ResponseEntity.ok(ApiResponse.success(Messages.Audit.FETCHED_TRAIL_SUCCESSFULLY, auditLogs));
    }

    @GetMapping("/action/{action}")
    @SandboxAuthorizedSync(action = "audit.read", object = "$$action$$@" + AuditLogsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<AuditLogsResponse>>> getAuditLogsByAction(
            @PathVariable("action") AuditAction action) {

        logger.info(FETCHING_AUDIT_TRAIL)
                .attr(ACTION, action)
                .log();

        List<AuditLogsResponse> auditLogs = auditService.getAuditLogsByAction(action);

        logger.info(Messages.Audit.FETCHED_TRAIL_SUCCESSFULLY)
                .attr(ACTION, action)
                .attr(Messages.Keys.LOG_COUNT, auditLogs.size())
                .log();

        return ResponseEntity.ok(ApiResponse.success(Messages.Audit.FETCHED_TRAIL_SUCCESSFULLY, auditLogs));
    }

    @GetMapping("/user/{userId}")
    @SandboxAuthorizedSync(action = "audit.read", object = "$$userId$$@" + AuditLogsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<AuditLogsResponse>>> getAuditLogsByUser(
            @PathVariable("userId") UUID userId) {

        logger.info(FETCHING_AUDIT_TRAIL)
                .attr(USER_ID, userId)
                .log();

        List<AuditLogsResponse> auditLogs = auditService.getAuditLogsByUser(userId);

        logger.info(Messages.Audit.FETCHED_LOGS_FOR_USER)
                .attr(USER_ID, userId)
                .attr(Messages.Keys.LOG_COUNT, auditLogs.size())
                .log();

        return ResponseEntity.ok(ApiResponse.success(Messages.Audit.FETCHED_LOGS_FOR_USER, auditLogs));
    }

    @GetMapping("/all")
    @SandboxAuthorizedSync(action = "audit.read", object = "$$tenants$$@" + AuditLogsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<AuditLogsResponse>>> getAllAuditLogs() {

        logger.info(FETCHING_ALL_AUDIT_LOGS)
                .log();

        List<AuditLogsResponse> auditLogs = auditService.getAllAuditLogs();

        logger.info(Messages.Audit.FETCHED_ALL_LOGS)
                .attr(LOG_COUNT, auditLogs.size())
                .log();

        return ResponseEntity.ok(ApiResponse.success(Messages.Audit.FETCHED_ALL_LOGS, auditLogs));
    }
}