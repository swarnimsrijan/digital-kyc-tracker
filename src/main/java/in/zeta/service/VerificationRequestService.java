package in.zeta.service;

import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.requests.CreateVerificationRequest;
import in.zeta.dto.requests.UpdateVerificationStatusRequest;
import in.zeta.dto.response.CreateVerificationResponse;
import in.zeta.dto.response.VerificationRequestResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

public interface VerificationRequestService {
    CreateVerificationResponse createVerificationRequest(@Valid CreateVerificationRequest createVerificationRequest, UUID requestorId);

    VerificationRequestResponse getVerificationRequest(UUID verificationId);

    List<VerificationRequestResponse> getCustomerVerifications(UUID customerId);

    List<VerificationRequestResponse> getOfficerVerifications(UUID officerId);

    List<VerificationRequestResponse> getRequestorVerifications(UUID requestorId);

    List<VerificationRequestResponse> getOpenVerifications();

    List<VerificationRequestResponse> getPendingRequestsAssignedToOfficer(UUID officerId);

    VerificationRequestResponse updateStatus(UUID verificationId, @Valid UpdateVerificationStatusRequest updateVerificationStatusRequest, UUID officerId);

    VerificationRequestResponse assignToOfficer(UUID verificationId, UUID officerId);

    VerificationStatus getVerificationStatus(UUID verificationId);

    Users getUserByVerificationRequest(UUID verificationId);

    VerificationRequest getByIdOrThrow(UUID verificationId);

    void saveVerificationRequest(VerificationRequest verificationRequest);

    List<VerificationRequest> findByAssignedOfficerAndStatus(Users officer, VerificationStatus status);

    void save(VerificationRequest verificationRequest);




}
