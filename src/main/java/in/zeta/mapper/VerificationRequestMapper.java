package in.zeta.mapper;

import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.response.CreateVerificationResponse;
import in.zeta.dto.response.VerificationRequestResponse;

import java.time.LocalDateTime;

public class VerificationRequestMapper {
    public static VerificationRequestResponse convertToResponse(VerificationRequest request) {
        return VerificationRequestResponse.builder()
                .id(request.getId())
                .customerId(request.getCustomer().getId())
                .customerName(request.getCustomer().getUsername())
                .requestorId(request.getRequestor().getId())
                .requestorName(request.getRequestor().getUsername())
                .assignedOfficerId(request.getAssignedOfficer() != null ? request.getAssignedOfficer().getId() : null)
                .assignedOfficerName(request.getAssignedOfficer() != null ? request.getAssignedOfficer().getUsername() : null)
                .status(request.getStatus())
                .requestReason(request.getRequestReason())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .approvedAt(request.getApprovedAt())
                .rejectedAt(request.getRejectedAt())
                .build();
    }

    public static CreateVerificationResponse convertToCreateResponse(VerificationRequest request) {
        return CreateVerificationResponse.builder()
                .id(request.getId())
                .customerId(request.getCustomer().getId())
                .customerName(request.getCustomer().getUsername())
                .requestorId(request.getRequestor().getId())
                .requestorName(request.getRequestor().getUsername())
                .status(request.getStatus())
                .requestReason(request.getRequestReason())
                .createdAt(request.getCreatedAt())
                .build();
    }

    public static VerificationRequest convertToEntity(VerificationRequest request) {
        return VerificationRequest.builder()
                .id(request.getId())
                .customer(request.getCustomer())
                .requestor(request.getRequestor())
                .assignedOfficer(request.getAssignedOfficer())
                .status(request.getStatus())
                .requestReason(request.getRequestReason())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .approvedAt(request.getApprovedAt())
                .rejectedAt(request.getRejectedAt())
                .build();
    }

    public static VerificationRequest createrequest(Users customer, Users requestor, String requestReason) {
        return VerificationRequest.builder()
                .customer(customer)
                .requestor(requestor)
                .status(VerificationStatus.PENDING)
                .requestReason(requestReason)
                .createdAt(LocalDateTime.now())
                .build();
    }
    private VerificationRequestMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
}
