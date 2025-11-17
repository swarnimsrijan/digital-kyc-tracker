package in.zeta.mapper;

import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequestLimit;
import in.zeta.dto.response.RequestorCountListResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VerificationRequestLimitMapperTest {

    @Test
    void mapToRequestorCountListResponses_success() {
        VerificationRequestLimit limit = createTestVerificationRequestLimit();

        RequestorCountListResponse response = VerificationRequestLimitMapper
                .mapToRequestorCountListResponses(limit);

        assertNotNull(response);
        assertEquals(limit.getVerificationRequestor().getId(), response.getRequestorId());
        assertEquals(limit.getCustomer().getId(), response.getCustomerId());
        assertEquals(limit.getVerificationRequestor().getUsername(), response.getRequestorName());
        assertEquals(limit.getVerificationRequestor().getEmail(), response.getRequestorEmail());
        assertEquals(limit.getRequestCount(), response.getRequestCount());
        assertEquals(limit.getMaxAllowedRequests(), response.getMaxAllowedRequests());
        assertEquals(limit.getYear(), response.getYear());
    }

    private VerificationRequestLimit createTestVerificationRequestLimit() {
        Users requestor = Users.builder()
                .id(UUID.randomUUID())
                .username("requestor")
                .email("requestor@test.com")
                .build();

        Users customer = Users.builder()
                .id(UUID.randomUUID())
                .username("customer")
                .email("customer@test.com")
                .build();

        return VerificationRequestLimit.builder()
                .verificationRequestor(requestor)
                .customer(customer)
                .requestCount(5)
                .maxAllowedRequests(100)
                .year(2024)
                .build();
    }
}