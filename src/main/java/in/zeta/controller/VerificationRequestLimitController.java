package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.dto.response.RequestCountResponse;
import in.zeta.dto.response.RequestorCountListResponse;
import in.zeta.service.VerificationRequestLimitService;
import in.zeta.spectra.capture.SpectraLogger;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/verification-request-limits")
@RequiredArgsConstructor
public class VerificationRequestLimitController {

    private final VerificationRequestLimitService verificationRequestLimitService;
    private final SpectraLogger logger = OlympusSpectra.getLogger(VerificationRequestLimitController.class);

    @GetMapping("/requestor/{requestorId}/customer/{customerId}/year/{year}")
    public ResponseEntity<RequestCountResponse> getRequestorRequestsToCustomerCurrentYear(
            @PathVariable("requestorId") UUID requestorId,
            @PathVariable("customerId") UUID customerId,
            @PathVariable("year") int year) {

        logger.info(Messages.VerificationRequestLimit.FETCHING_REQUESTOR_REQUESTS_TO_CUSTOMER)
                .attr(Messages.Keys.YEAR, year)
                .attr(Messages.Keys.REQUESTOR_ID, requestorId)
                .attr(Messages.Keys.CUSTOMER_ID, customerId)
                .log();

        RequestCountResponse response = verificationRequestLimitService
                .getRequestorRequestsToCustomerCurrentYear(requestorId, customerId, year);

        logger.info(Messages.VerificationRequestLimit.FETCHED_REQUESTOR_REQUESTS_TO_CUSTOMER)
                .attr(Messages.Keys.YEAR, year)
                .attr(Messages.Keys.REQUESTOR_ID, requestorId)
                .attr(Messages.Keys.CUSTOMER_ID, customerId)
                .attr(Messages.Keys.REQUEST_COUNT, response.getRequestCount())
                .attr(Messages.Keys.MAX_ALLOWED_REQUESTS, response.getMaxAllowedRequests())
                .log();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}/total-requests/current-year")
    public ResponseEntity<RequestCountResponse> getTotalRequestsToCustomerCurrentYear(
            @PathVariable("customerId") UUID customerId) {

        logger.info(Messages.VerificationRequestLimit.FETCHING_TOTAL_REQUESTS_TO_CUSTOMER)
                .attr(Messages.Keys.CUSTOMER_ID, customerId)
                .log();

        RequestCountResponse response = verificationRequestLimitService
                .getTotalRequestsToCustomerCurrentYear(customerId);

        logger.info("Messages.VerificationRequestLimit.FETCHED_TOTAL_REQUESTS_TO_CUSTOMER")
                .attr(Messages.Keys.CUSTOMER_ID, customerId)
                .attr(Messages.Keys.REQUEST_COUNT, response.getRequestCount())
                .attr(Messages.Keys.MAX_ALLOWED_REQUESTS, response.getMaxAllowedRequests())
                .log();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}/requestors-count")
    public ResponseEntity<List<RequestorCountListResponse>> getAllRequestorsCountForCustomer(
            @PathVariable("customerId") UUID customerId) {

        logger.info(Messages.VerificationRequestLimit.FETCHING_TOTAL_REQUESTS_TO_CUSTOMER)
                .attr(Messages.Keys.CUSTOMER_ID, customerId)
                .log();

        List<RequestorCountListResponse> response = verificationRequestLimitService
                .getAllRequestorsCountForCustomer(customerId);
        logger.info(Messages.VerificationRequestLimit.FETCHED_TOTAL_REQUESTS_TO_CUSTOMER)
                .attr(Messages.Keys.CUSTOMER_ID, customerId)
                .attr(Messages.Keys.RESPONSE_SIZE, response.size())
                .log();

        return ResponseEntity.ok(response);
    }
}