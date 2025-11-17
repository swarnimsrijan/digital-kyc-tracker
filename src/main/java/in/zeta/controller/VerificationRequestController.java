package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.enums.VerificationStatus;
import in.zeta.providers.VerificationRequestsProvider;
import in.zeta.dto.requests.CreateVerificationRequest;
import in.zeta.dto.requests.UpdateVerificationStatusRequest;
import in.zeta.dto.response.ApiResponse;
import in.zeta.dto.response.CreateVerificationResponse;
import in.zeta.dto.response.VerificationRequestResponse;
import in.zeta.service.VerificationRequestService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAuthorizedSync;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import static in.zeta.constants.Messages.Keys.*;
import static in.zeta.constants.Messages.Verification.*;
import static in.zeta.enums.VerificationStatus.*;


@RestController
@RequestMapping("/tenants/{tenantId}/verification")
@RequiredArgsConstructor
public class VerificationRequestController {

    private final VerificationRequestService verificationRequestService;
    private final SpectraLogger logger = OlympusSpectra.getLogger(VerificationRequestController.class);

    @PostMapping("/verification-request/{requestorId}")
    @SandboxAuthorizedSync(action = "request.create", object = "$$requestorId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<CreateVerificationResponse>> createVerificationRequest(
            @Valid @RequestBody CreateVerificationRequest createVerificationRequest,
            @PathVariable UUID requestorId) {

        logger.info(CREATING_VERIFICATION_REQUEST)
                .attr(Messages.Keys.REQUESTOR_ID, requestorId)
                .attr(CUSTOMER_ID, createVerificationRequest.getCustomerId())
                .log();

        CreateVerificationResponse response = verificationRequestService
                .createVerificationRequest(createVerificationRequest, requestorId);

        logger.info(VERIFICATION_REQUEST_CREATED)
                .attr(Messages.Keys.VERIFICATION_ID, response.getId())
                .log();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Messages.Verification.VERIFICATION_REQUEST_CREATED_SUCCESSFULLY, response));
    }

    @PostMapping("/verification-requests/{requestorId}")
    @SandboxAuthorizedSync(action = "request.create", object = "$$requestorId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<CreateVerificationResponse>>> createMultipleVerificationRequests(
            @Valid @RequestBody List<CreateVerificationRequest> createVerificationRequests,
            @PathVariable UUID requestorId) {

        logger.info(CREATING_MULTIPLE_VERIFICATION_REQUESTS)
                .attr(REQUESTOR_ID, requestorId)
                .attr(VERIFICATION_REQUEST_COUNT, createVerificationRequests.size())
                .log();

        List<CreateVerificationResponse> responses = createVerificationRequests.stream()
                .map(request -> verificationRequestService
                        .createVerificationRequest(request, requestorId))
                .toList();

        logger.info(MULTIPLE_VERIFICATION_REQUESTS_CREATED)
                .attr(VERIFICATION_REQUEST_COUNT, responses.size())
                .attr(REQUESTOR_ID, requestorId)
                .log();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(VERIFICATION_REQUEST_CREATED_FOR_MULTIPLE_CUSTOMER_SUCCESSFULLY, responses));
    }

    @GetMapping("/verification-request/{requestorId}")
    @SandboxAuthorizedSync(action = "request.read", object = "$$requestorId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<VerificationRequestResponse>>> getVerificationsByRequestor(
            @PathVariable UUID requestorId) {

        logger.info(FETCHING_VERIFICATION_REQUESTS_BY_REQUESTOR)
                .attr(REQUESTOR_ID, requestorId)
                .log();

        List<VerificationRequestResponse> response = verificationRequestService
                .getRequestorVerifications(requestorId);

        logger.info("Fetched ")
                .attr(VERIFICATION_REQUEST_COUNT, response.size())
                .attr(REQUESTOR_ID, requestorId)
                .log();

        return ResponseEntity.ok(ApiResponse.success(ALL_VERIFICATION_REQUESTS_BY_REQUESTOR,response));
    }

    @GetMapping("/{verificationId}")
    @SandboxAuthorizedSync(action = "request.read", object = "$$verificationId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<VerificationRequestResponse>> getVerificationRequest(
            @PathVariable UUID verificationId) {

        logger.info(FETCHED_VERIFICATION_REQUESTS_BY_REQUESTOR)
                .attr(VERIFICATION_ID, verificationId)
                .log();

        VerificationRequestResponse response = verificationRequestService
                .getVerificationRequest(verificationId);

        logger.info(FETCHING_VERIFICATION_REQUEST_BY_ID)
                .attr(VERIFICATION_ID, verificationId)
                .log();

        return ResponseEntity.ok(ApiResponse.success(VERIFICATION_REQUEST_FOR_VERIFICATION_ID, response));
    }

    @GetMapping("/customer/{customerId}")
    @SandboxAuthorizedSync(action = "request.create", object = "$$customerId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<VerificationRequestResponse>>> getCustomerVerifications(
            @PathVariable UUID customerId) {

        logger.info(FETCHED_VERIFICATION_REQUEST_BY_ID)
                .attr(CUSTOMER_ID, customerId)
                .log();

        List<VerificationRequestResponse> response = verificationRequestService
                .getCustomerVerifications(customerId);

        logger.info("Fetched ")
                .attr(VERIFICATION_REQUEST_COUNT, response.size())
                .attr(CUSTOMER_ID, customerId)
                .log();

        return ResponseEntity.ok(ApiResponse.success(VERIFICATION_REQUESTS_FOR_CUSTOMER, response));
    }

    @GetMapping("/officer/{officerId}")
    @SandboxAuthorizedSync(action = "request.read", object = "$$requestorId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<VerificationRequestResponse>>> getOfficerVerifications(
            @PathVariable UUID officerId) {

        logger.info(FETCHING_VERIFICATION_REQUESTS_FOR_OFFICER)
                .attr(OFFICER_ID, officerId)
                .log();

        List<VerificationRequestResponse> response = verificationRequestService
                .getOfficerVerifications(officerId);

        logger.info("Fetched ")
                .attr(VERIFICATION_REQUEST_COUNT, response.size())
                .attr(OFFICER_ID, officerId)
                .log();

        return ResponseEntity.ok(ApiResponse.success(ASSIGNING_TO_OFFICER, response));
    }

    @GetMapping("/verification-requests/pending")
    @SandboxAuthorizedSync(action = "request.read", object = "$$requestorId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<VerificationRequestResponse>>> getPendingVerifications() {

        logger.info(FETCHING_PENDING_VERIFICATION_REQUESTS)
                .attr(STATUS, PENDING)
                .log();

        List<VerificationRequestResponse> response = verificationRequestService
                .getOpenVerifications();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{verificationId}/assign/{officerId}")
    @SandboxAuthorizedSync(action = "request.update", object = "$$requestorId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<VerificationRequestResponse>> assignToOfficer(
            @PathVariable UUID verificationId,
            @PathVariable UUID officerId) {

        VerificationRequestResponse response = verificationRequestService
                .assignToOfficer(verificationId, officerId);

        return ResponseEntity.ok(ApiResponse.success(ASSIGNED_VERIFICATION_REQUEST_SUCCESSFULLY, response));
    }

    @PutMapping("/{verificationId}/status/{officerId}")
    @SandboxAuthorizedSync(action = "request.update", object = "$$requestorId$$@" + VerificationRequestsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<VerificationRequestResponse>> updateStatus(
            @PathVariable UUID verificationId,
            @PathVariable UUID officerId,
            @Valid @RequestBody UpdateVerificationStatusRequest updateVerificationStatusRequest) {

        VerificationStatus oldStatus = verificationRequestService
                .getVerificationStatus(verificationId);

        logger.info(UPDATING_VERIFICATION_STATUS)
                .attr(VERIFICATION_ID, verificationId)
                .attr("oldStatus", oldStatus)
                .attr("newStatus: ", updateVerificationStatusRequest.getStatus())
                .log();

        VerificationRequestResponse response = verificationRequestService
                .updateStatus(verificationId, updateVerificationStatusRequest, officerId);

        logger.info(UPDATED_VERIFICATION_STATUS)
                .attr(VERIFICATION_ID, verificationId)
                .attr("oldStatus", oldStatus)
                .attr("newStatus: ", response.getStatus())
                .log();

        return ResponseEntity.ok(ApiResponse.success(STATUS_UPDATED_SUCCESSFULLY, response));
    }

}