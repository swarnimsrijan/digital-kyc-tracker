package in.zeta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.constants.Messages;
import in.zeta.dto.requests.CreateVerificationRequest;
import in.zeta.dto.requests.UpdateVerificationStatusRequest;
import in.zeta.dto.response.CreateVerificationResponse;
import in.zeta.dto.response.VerificationRequestResponse;
import in.zeta.enums.VerificationStatus;
import in.zeta.service.VerificationRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VerificationRequestController.class)
class VerificationRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificationRequestService verificationRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID requestorId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID verificationId = UUID.randomUUID();
    private final UUID officerId = UUID.randomUUID();

    @Test
    void testCreateVerificationRequest_Success() throws Exception {

        CreateVerificationRequest request = CreateVerificationRequest.builder()
                .customerId(customerId)
                .requestReason("KYC verification required")
                .build();

        CreateVerificationResponse response = CreateVerificationResponse.builder()
                .id(verificationId)
                .customerId(customerId)
                .requestorId(requestorId)
                .status(VerificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(verificationRequestService.createVerificationRequest(any(CreateVerificationRequest.class), eq(requestorId)))
                .thenReturn(response);


        mockMvc.perform(post("/tenants/{tenantId}/verification/verification-request/{requestorId}",
                        tenantId, requestorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.VERIFICATION_REQUEST_CREATED_SUCCESSFULLY))
                .andExpect(jsonPath("$.data.id").value(verificationId.toString()))
                .andExpect(jsonPath("$.data.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.data.status").value(VerificationStatus.PENDING.name()));

        verify(verificationRequestService).createVerificationRequest(any(CreateVerificationRequest.class), eq(requestorId));
    }

    @Test
    void testCreateMultipleVerificationRequests_Success() throws Exception {

        List<CreateVerificationRequest> requests = Arrays.asList(
                CreateVerificationRequest.builder()
                        .customerId(UUID.randomUUID())
                        .requestReason("KYC verification 1")
                        .build(),
                CreateVerificationRequest.builder()
                        .customerId(UUID.randomUUID())
                        .requestReason("KYC verification 2")
                        .build()
        );

        CreateVerificationResponse response1 = CreateVerificationResponse.builder()
                .id(UUID.randomUUID())
                .customerId(requests.get(0).getCustomerId())
                .requestorId(requestorId)
                .status(VerificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        CreateVerificationResponse response2 = CreateVerificationResponse.builder()
                .id(UUID.randomUUID())
                .customerId(requests.get(1).getCustomerId())
                .requestorId(requestorId)
                .status(VerificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(verificationRequestService.createVerificationRequest(eq(requests.get(0)), eq(requestorId)))
                .thenReturn(response1);
        when(verificationRequestService.createVerificationRequest(eq(requests.get(1)), eq(requestorId)))
                .thenReturn(response2);

        mockMvc.perform(post("/tenants/{tenantId}/verification/verification-requests/{requestorId}",
                        tenantId, requestorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.VERIFICATION_REQUEST_CREATED_FOR_MULTIPLE_CUSTOMER_SUCCESSFULLY))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(verificationRequestService).createVerificationRequest(eq(requests.get(0)), eq(requestorId));
        verify(verificationRequestService).createVerificationRequest(eq(requests.get(1)), eq(requestorId));
    }

    @Test
    void testGetVerificationsByRequestor_Success() throws Exception {
        List<VerificationRequestResponse> responses = Arrays.asList(
                VerificationRequestResponse.builder()
                        .id(verificationId)
                        .customerId(customerId)
                        .requestorId(requestorId)
                        .status(VerificationStatus.IN_REVIEW)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(verificationRequestService.getRequestorVerifications(requestorId)).thenReturn(responses);

        mockMvc.perform(get("/tenants/{tenantId}/verification/verification-request/{requestorId}",
                        tenantId, requestorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.ALL_VERIFICATION_REQUESTS_BY_REQUESTOR))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].requestorId").value(requestorId.toString()));

        verify(verificationRequestService).getRequestorVerifications(requestorId);
    }

    @Test
    void testGetVerificationRequest_Success() throws Exception {
        VerificationRequestResponse response = VerificationRequestResponse.builder()
                .id(verificationId)
                .customerId(customerId)
                .requestorId(requestorId)
                .status(VerificationStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();

        when(verificationRequestService.getVerificationRequest(verificationId)).thenReturn(response);

        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.VERIFICATION_REQUEST_FOR_VERIFICATION_ID))
                .andExpect(jsonPath("$.data.id").value(verificationId.toString()))
                .andExpect(jsonPath("$.data.status").value(VerificationStatus.APPROVED.name()));

        verify(verificationRequestService).getVerificationRequest(verificationId);
    }

    @Test
    void testGetPendingVerifications_Success() throws Exception {
        List<VerificationRequestResponse> responses = Arrays.asList(
                VerificationRequestResponse.builder()
                        .id(UUID.randomUUID())
                        .customerId(UUID.randomUUID())
                        .requestorId(UUID.randomUUID())
                        .status(VerificationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build(),
                VerificationRequestResponse.builder()
                        .id(UUID.randomUUID())
                        .customerId(UUID.randomUUID())
                        .requestorId(UUID.randomUUID())
                        .status(VerificationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(verificationRequestService.getOpenVerifications()).thenReturn(responses);
        mockMvc.perform(get("/tenants/{tenantId}/verification/verification-requests/pending", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").value(VerificationStatus.PENDING.name()))
                .andExpect(jsonPath("$.data[1].status").value(VerificationStatus.PENDING.name()));

        verify(verificationRequestService).getOpenVerifications();
    }

    @Test
    void testAssignToOfficer_Success() throws Exception {
        VerificationRequestResponse response = VerificationRequestResponse.builder()
                .id(verificationId)
                .customerId(customerId)
                .requestorId(requestorId)
                .assignedOfficerId(officerId)
                .status(VerificationStatus.IN_REVIEW)
                .createdAt(LocalDateTime.now())
                .build();

        when(verificationRequestService.assignToOfficer(verificationId, officerId)).thenReturn(response);
        mockMvc.perform(put("/tenants/{tenantId}/verification/{verificationId}/assign/{officerId}",
                        tenantId, verificationId, officerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.ASSIGNED_VERIFICATION_REQUEST_SUCCESSFULLY))
                .andExpect(jsonPath("$.data.assignedOfficerId").value(officerId.toString()));

        verify(verificationRequestService).assignToOfficer(verificationId, officerId);
    }

    @Test
    void testUpdateStatus_Success() throws Exception {
        UpdateVerificationStatusRequest request = UpdateVerificationStatusRequest.builder()
                .status(VerificationStatus.APPROVED)
                .reason("All documents verified")
                .build();

        VerificationRequestResponse response = VerificationRequestResponse.builder()
                .id(verificationId)
                .customerId(customerId)
                .requestorId(requestorId)
                .status(VerificationStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();

        when(verificationRequestService.getVerificationStatus(verificationId)).thenReturn(VerificationStatus.IN_REVIEW);
        when(verificationRequestService.updateStatus(eq(verificationId), any(UpdateVerificationStatusRequest.class), eq(officerId)))
                .thenReturn(response);

        mockMvc.perform(put("/tenants/{tenantId}/verification/{verificationId}/status/{officerId}",
                        tenantId, verificationId, officerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.STATUS_UPDATED_SUCCESSFULLY))
                .andExpect(jsonPath("$.data.status").value(VerificationStatus.APPROVED.name()));

        verify(verificationRequestService).getVerificationStatus(verificationId);
        verify(verificationRequestService).updateStatus(eq(verificationId), any(UpdateVerificationStatusRequest.class), eq(officerId));
    }

    @Test
    void testCreateVerificationRequest_InvalidRequest() throws Exception {
        CreateVerificationRequest request = CreateVerificationRequest.builder()
                .requestReason("")
                .build();

        mockMvc.perform(post("/tenants/{tenantId}/verification/verification-request/{requestorId}",
                        tenantId, requestorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCustomerVerifications_Success() throws Exception {
        List<VerificationRequestResponse> responses = Arrays.asList(
                VerificationRequestResponse.builder()
                        .id(verificationId)
                        .customerId(customerId)
                        .requestorId(requestorId)
                        .status(VerificationStatus.DOCUMENT_UPLOADED)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(verificationRequestService.getCustomerVerifications(customerId)).thenReturn(responses);

        mockMvc.perform(get("/tenants/{tenantId}/verification/customer/{customerId}", tenantId, customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.VERIFICATION_REQUESTS_FOR_CUSTOMER))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].customerId").value(customerId.toString()));

        verify(verificationRequestService).getCustomerVerifications(customerId);
    }

    @Test
    void testGetOfficerVerifications_Success() throws Exception {
        List<VerificationRequestResponse> responses = Arrays.asList(
                VerificationRequestResponse.builder()
                        .id(verificationId)
                        .customerId(customerId)
                        .requestorId(requestorId)
                        .assignedOfficerId(officerId)
                        .status(VerificationStatus.IN_REVIEW)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(verificationRequestService.getOfficerVerifications(officerId)).thenReturn(responses);

        mockMvc.perform(get("/tenants/{tenantId}/verification/officer/{officerId}", tenantId, officerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.ASSIGNING_TO_OFFICER))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].assignedOfficerId").value(officerId.toString()));

        verify(verificationRequestService).getOfficerVerifications(officerId);
    }
}