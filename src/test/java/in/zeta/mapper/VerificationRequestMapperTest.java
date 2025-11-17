package in.zeta.mapper;

import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.response.CreateVerificationResponse;
import in.zeta.dto.response.VerificationRequestResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VerificationRequestMapperTest {

    @Test
    void convertToResponse_success() {
        Users customer = Users.builder().id(UUID.randomUUID()).username("customer").build();
        Users requestor = Users.builder().id(UUID.randomUUID()).username("requestor").build();
        Users officer = Users.builder().id(UUID.randomUUID()).username("officer").build();
        VerificationRequest request = VerificationRequest.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .requestor(requestor)
                .assignedOfficer(officer)
                .status(VerificationStatus.PENDING)
                .requestReason("reason")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.now())
                .rejectedAt(null)
                .build();

        VerificationRequestResponse response = VerificationRequestMapper.convertToResponse(request);

        assertNotNull(response);
        assertEquals(request.getId(), response.getId());
        assertEquals(customer.getId(), response.getCustomerId());
        assertEquals(customer.getUsername(), response.getCustomerName());
        assertEquals(requestor.getId(), response.getRequestorId());
        assertEquals(requestor.getUsername(), response.getRequestorName());
        assertEquals(officer.getId(), response.getAssignedOfficerId());
        assertEquals(officer.getUsername(), response.getAssignedOfficerName());
        assertEquals(request.getStatus(), response.getStatus());
        assertEquals(request.getRequestReason(), response.getRequestReason());
        assertEquals(request.getCreatedAt(), response.getCreatedAt());
        assertEquals(request.getUpdatedAt(), response.getUpdatedAt());
        assertEquals(request.getApprovedAt(), response.getApprovedAt());
        assertEquals(request.getRejectedAt(), response.getRejectedAt());
    }

    @Test
    void convertToCreateResponse_success() {
        Users customer = Users.builder().id(UUID.randomUUID()).username("customer").build();
        Users requestor = Users.builder().id(UUID.randomUUID()).username("requestor").build();
        VerificationRequest request = VerificationRequest.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .requestor(requestor)
                .status(VerificationStatus.PENDING)
                .requestReason("reason")
                .createdAt(LocalDateTime.now())
                .build();

        CreateVerificationResponse response = VerificationRequestMapper.convertToCreateResponse(request);

        assertNotNull(response);
        assertEquals(request.getId(), response.getId());
        assertEquals(customer.getId(), response.getCustomerId());
        assertEquals(customer.getUsername(), response.getCustomerName());
        assertEquals(requestor.getId(), response.getRequestorId());
        assertEquals(requestor.getUsername(), response.getRequestorName());
        assertEquals(request.getStatus(), response.getStatus());
        assertEquals(request.getRequestReason(), response.getRequestReason());
        assertEquals(request.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void convertToEntity_success() {
        Users customer = Users.builder().id(UUID.randomUUID()).username("customer").build();
        Users requestor = Users.builder().id(UUID.randomUUID()).username("requestor").build();
        Users officer = Users.builder().id(UUID.randomUUID()).username("officer").build();
        VerificationRequest request = VerificationRequest.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .requestor(requestor)
                .assignedOfficer(officer)
                .status(VerificationStatus.PENDING)
                .requestReason("reason")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.now())
                .rejectedAt(null)
                .build();

        VerificationRequest entity = VerificationRequestMapper.convertToEntity(request);

        assertNotNull(entity);
        assertEquals(request.getId(), entity.getId());
        assertEquals(customer, entity.getCustomer());
        assertEquals(requestor, entity.getRequestor());
        assertEquals(officer, entity.getAssignedOfficer());
        assertEquals(request.getStatus(), entity.getStatus());
        assertEquals(request.getRequestReason(), entity.getRequestReason());
        assertEquals(request.getCreatedAt(), entity.getCreatedAt());
        assertEquals(request.getUpdatedAt(), entity.getUpdatedAt());
        assertEquals(request.getApprovedAt(), entity.getApprovedAt());
        assertEquals(request.getRejectedAt(), entity.getRejectedAt());
    }

    @Test
    void createrequest_success() {
        Users customer = Users.builder().id(UUID.randomUUID()).username("customer").build();
        Users requestor = Users.builder().id(UUID.randomUUID()).username("requestor").build();
        String reason = "reason";
        VerificationRequest request = VerificationRequestMapper.createrequest(customer, requestor, reason);

        assertNotNull(request);
        assertEquals(customer, request.getCustomer());
        assertEquals(requestor, request.getRequestor());
        assertEquals(VerificationStatus.PENDING, request.getStatus());
        assertEquals(reason, request.getRequestReason());
        assertNotNull(request.getCreatedAt());
    }
}