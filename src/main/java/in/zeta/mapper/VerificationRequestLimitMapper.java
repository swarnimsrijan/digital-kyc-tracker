package in.zeta.mapper;

import in.zeta.entity.VerificationRequestLimit;
import in.zeta.dto.response.RequestorCountListResponse;

public class VerificationRequestLimitMapper {
    public static RequestorCountListResponse mapToRequestorCountListResponses(VerificationRequestLimit verificationRequestLimit) {
        return RequestorCountListResponse.builder()
                .requestorId(verificationRequestLimit.getVerificationRequestor().getId())
                .customerId(verificationRequestLimit.getCustomer().getId())
                .requestorName(verificationRequestLimit.getVerificationRequestor().getUsername())
                .requestorEmail(verificationRequestLimit.getVerificationRequestor().getEmail())
                .requestCount(verificationRequestLimit.getRequestCount())
                .maxAllowedRequests(verificationRequestLimit.getMaxAllowedRequests())
                .year(verificationRequestLimit.getYear())
                .build();
    }
    private VerificationRequestLimitMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
}