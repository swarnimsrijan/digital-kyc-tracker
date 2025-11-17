package in.zeta.service;

import in.zeta.dto.response.RequestCountResponse;
import in.zeta.dto.response.RequestorCountListResponse;
import in.zeta.entity.Users;
import java.util.List;
import java.util.UUID;

public interface VerificationRequestLimitService {
    boolean canCreateVerificationRequest(Users customer, Users requestor);
    void incrementRequestCount(Users customer, Users requestor);

    RequestCountResponse getRequestorRequestsToCustomerCurrentYear(UUID requestorId, UUID customerId, int year);
    RequestCountResponse getTotalRequestsToCustomerCurrentYear(UUID customerId);
    List<RequestorCountListResponse> getAllRequestorsCountForCustomer(UUID customerId);
}
