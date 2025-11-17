package in.zeta.service.impl;

import in.zeta.dto.response.VerificationRequestResponse;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.entity.VerificationRequestLimit;
import in.zeta.exception.DataNotFoundException;
import in.zeta.mapper.VerificationRequestLimitMapper;
import in.zeta.repository.VerificationRequestLimitRepository;
import in.zeta.repository.VerificationRequestRepository;
import in.zeta.dto.response.RequestCountResponse;
import in.zeta.dto.response.RequestorCountListResponse;
import in.zeta.service.UserService;
import in.zeta.service.VerificationRequestLimitService;
import in.zeta.spectra.capture.SpectraLogger;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VerificationRequestLimitServiceImpl implements VerificationRequestLimitService {

    private final VerificationRequestLimitRepository verificationRequestLimitRepository;
    private final UserService userService;


    private static final SpectraLogger logger = OlympusSpectra.getLogger(VerificationRequestLimitServiceImpl.class);

    @Value("${max.verification.requestor.requests.per.year}")
    private Integer defaultMaxAllowedRequests;

    @Value("${max.verification.request.to.each.customer}")
    private Integer maxVerificationRequestsToEachCustomer;

    @Override
    public boolean canCreateVerificationRequest(Users customer, Users requestor) {
        int currentYear = LocalDate.now().getYear();

        Optional<VerificationRequestLimit> record =
                verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                        customer.getId(), requestor.getId(), currentYear);

        if (record.isEmpty()) {
            return true;
        }

        VerificationRequestLimit limit = record.get();
        int currentCount = limit.getRequestCount() != null ? limit.getRequestCount() : 0;
        int maxAllowed = limit.getMaxAllowedRequests() != null ? limit.getMaxAllowedRequests() : maxVerificationRequestsToEachCustomer;
        int totalRequests = limit.getTotalRequests() != null ? limit.getTotalRequests() : 0;

        boolean canCreate = (currentCount < defaultMaxAllowedRequests && totalRequests < maxAllowed);

        logger.info("Checking verification request limit")
                .attr("customerId", customer.getId())
                .attr("requestorId", requestor.getId())
                .attr("currentCount", currentCount)
                .attr("maxAllowed", maxAllowed)
                .attr("canCreate", canCreate)
                .log();

        return canCreate;
    }

    @Override
    public void incrementRequestCount(Users customer, Users requestor) {
        int currentYear = LocalDate.now().getYear();

        // Find existing record
        Optional<VerificationRequestLimit> existingRecord =
                verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                        customer.getId(), requestor.getId(), currentYear);

        VerificationRequestLimit record;

        if (existingRecord.isPresent()) {
            // Update existing record
            record = existingRecord.get();
            int currentCount = record.getRequestCount() != null ? record.getRequestCount() : 0;
            int currentTotal = record.getTotalRequests() != null ? record.getTotalRequests() : 0;
            if(currentCount <= defaultMaxAllowedRequests){
                record.setRequestCount(currentCount + 1);
            }else{
                record.setRequestCount(1);
            }
            record.setTotalRequests(currentTotal + 1);

            logger.info("Incrementing existing request count")
                    .attr("customerId", customer.getId())
                    .attr("requestorId", requestor.getId())
                    .attr("previousCount", currentCount)
                    .attr("newCount", currentCount + 1)
                    .log();
        } else {
            // Create new record
            record = VerificationRequestLimit.builder()
                    .customer(customer)
                    .verificationRequestor(requestor)
                    .year(currentYear)
                    .requestCount(1)
                    .totalRequests(1)
                    .maxAllowedRequests(maxVerificationRequestsToEachCustomer) // Default or fetch from config
                    .createdAt(LocalDateTime.now())
                    .build();

            logger.info("Creating new request count record")
                    .attr("customerId", customer.getId())
                    .attr("requestorId", requestor.getId())
                    .attr("initialCount", 1)
                    .log();
        }

        verificationRequestLimitRepository.save(record);

        logger.info("Request count updated successfully")
                .attr("customerId", customer.getId())
                .attr("requestorId", requestor.getId())
                .attr("finalCount", record.getRequestCount())
                .log();
    }

    @Override
    public RequestCountResponse getRequestorRequestsToCustomerCurrentYear(UUID requestorId, UUID customerId, int year) {
        try {
            VerificationRequestLimit limitRecord =
                    verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                            customerId, requestorId, year
                    ).orElseThrow(() -> new DataNotFoundException("Data not found",
                            "customer_id_requestor_id_year",
                            String.format("%s_%s_%d", customerId, requestorId, year)));

            return RequestCountResponse.builder()
                    .customerId(customerId)
                    .year(year)
                    .requestCount(limitRecord.getRequestCount())
                    .maxAllowedRequests(limitRecord.getMaxAllowedRequests())
                    .build();
        } catch (DataNotFoundException e) {
            logger.error("Verification request limit record not found")
                    .attr("tableName", e.getTableName())
                    .attr("fieldName", e.getFieldName())
                    .attr("fieldValue", e.getFieldValue().toString())
                    .attr("requestorId", requestorId.toString())
                    .attr("customerId", customerId.toString())
                    .attr("year", year)
                    .log();
            throw e;
        }
    }

    @Override
    public RequestCountResponse getTotalRequestsToCustomerCurrentYear(UUID customerId) {
        int currentYear = LocalDate.now().getYear();
        Users user = userService.getByIdOrThrow(customerId, "Customer not found");
        Integer total = verificationRequestLimitRepository.getTotalRequestsForCustomerInYear(customerId, currentYear);

        if (total == null || total == 0) {
            throw new DataNotFoundException("verification_request_limit", "customer_id_year",
                    String.format("%s_%d", customerId, currentYear));
        }

        return RequestCountResponse.builder()
                .customerId(customerId)
                .year(currentYear)
                .customerName(user.getUsername())
                .requestCount(total)
                .maxAllowedRequests(defaultMaxAllowedRequests)
                .build();
    }

    @Override
    public List<RequestorCountListResponse> getAllRequestorsCountForCustomer(UUID customerId) {
        int year = LocalDate.now().getYear();

        List<VerificationRequestLimit> results =
                verificationRequestLimitRepository.findByCustomerIdAndYear(customerId, year);

        if (results == null || results.isEmpty()) {
            throw new DataNotFoundException("verification_request_limit", "customer_id_year",
                    String.format("%s_%d", customerId, year));
        }

        return results.stream()
                .map(VerificationRequestLimitMapper::mapToRequestorCountListResponses)
                .collect(Collectors.toList());
    }
}
