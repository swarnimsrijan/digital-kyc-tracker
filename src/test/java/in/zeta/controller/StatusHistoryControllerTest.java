package in.zeta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.constants.Messages;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.response.StatusHistoryResponse;
import in.zeta.service.StatusHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatusHistoryController.class)
@TestPropertySource(properties = {
        "spring.cloud.sleuth.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class StatusHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatusHistoryService statusHistoryService;

    private UUID tenantId;
    private UUID verificationId;
    private List<StatusHistoryResponse> mockStatusHistory;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        verificationId = UUID.randomUUID();
        mockStatusHistory = createMockStatusHistory();
    }

    private List<StatusHistoryResponse> createMockStatusHistory() {
        List<StatusHistoryResponse> history = new ArrayList<>();

        StatusHistoryResponse history1 = StatusHistoryResponse.builder()
                .id(UUID.randomUUID())
                .verificationRequestId(verificationId)
                .fromStatus(VerificationStatus.PENDING)
                .toStatus(VerificationStatus.IN_REVIEW)
                .changedAt(LocalDateTime.now().minusDays(2))
                .changedBy(UUID.randomUUID())
                .reason("Starting verification process")
                .build();

        StatusHistoryResponse history2 = StatusHistoryResponse.builder()
                .id(UUID.randomUUID())
                .verificationRequestId(verificationId)
                .fromStatus(VerificationStatus.IN_REVIEW)
                .toStatus(VerificationStatus.APPROVED)
                .changedAt(LocalDateTime.now().minusDays(1))
                .changedBy(UUID.randomUUID())
                .reason("All documents verified")
                .build();

        history.add(history1);
        history.add(history2);
        return history;
    }

    @Test
    void testGetVerificationStatus_Success() throws Exception {
        VerificationStatus expectedStatus = VerificationStatus.APPROVED;
        when(statusHistoryService.getLatestStatus(verificationId)).thenReturn(expectedStatus);
        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Verification.VERIFICATION_STATUS))
                .andExpect(jsonPath("$.data").value(expectedStatus.name()));

        verify(statusHistoryService).getLatestStatus(verificationId);
    }

    @Test
    void testGetVerificationStatus_PendingStatus() throws Exception {
        VerificationStatus expectedStatus = VerificationStatus.PENDING;
        when(statusHistoryService.getLatestStatus(verificationId)).thenReturn(expectedStatus);

        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(expectedStatus.name()));

        verify(statusHistoryService).getLatestStatus(verificationId);
    }

    @Test
    void testGetVerificationStatus_RejectedStatus() throws Exception {
        VerificationStatus expectedStatus = VerificationStatus.REJECTED;
        when(statusHistoryService.getLatestStatus(verificationId)).thenReturn(expectedStatus);

        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(expectedStatus.name()));

        verify(statusHistoryService).getLatestStatus(verificationId);
    }

    @Test
    void testGetStatusHistory_Success() throws Exception {
        when(statusHistoryService.getStatusHistoryByVerificationId(verificationId)).thenReturn(mockStatusHistory);
        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status/history", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Status.FETCHED_STATUS_HISTORY))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].verificationRequestId").value(verificationId.toString()))
                .andExpect(jsonPath("$.data[0].fromStatus").value(VerificationStatus.PENDING.name()))
                .andExpect(jsonPath("$.data[0].toStatus").value(VerificationStatus.IN_REVIEW.name()))
                .andExpect(jsonPath("$.data[0].reason").value("Starting verification process"))
                .andExpect(jsonPath("$.data[1].fromStatus").value(VerificationStatus.IN_REVIEW.name()))
                .andExpect(jsonPath("$.data[1].toStatus").value(VerificationStatus.APPROVED.name()))
                .andExpect(jsonPath("$.data[1].reason").value("All documents verified"));

        verify(statusHistoryService).getStatusHistoryByVerificationId(verificationId);
    }

    @Test
    void testGetStatusHistory_EmptyHistory() throws Exception {
        List<StatusHistoryResponse> emptyHistory = new ArrayList<>();
        when(statusHistoryService.getStatusHistoryByVerificationId(verificationId)).thenReturn(emptyHistory);

        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status/history", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(statusHistoryService).getStatusHistoryByVerificationId(verificationId);
    }

    @Test
    void testGetStatusHistory_SingleEntry() throws Exception {
        List<StatusHistoryResponse> singleHistory = new ArrayList<>();
        StatusHistoryResponse singleEntry = StatusHistoryResponse.builder()
                .id(UUID.randomUUID())
                .verificationRequestId(verificationId)
                .fromStatus(VerificationStatus.PENDING)
                .toStatus(VerificationStatus.DOCUMENT_UPLOADED)
                .changedAt(LocalDateTime.now())
                .changedBy(UUID.randomUUID())
                .reason("Documents uploaded by customer")
                .build();
        singleHistory.add(singleEntry);

        when(statusHistoryService.getStatusHistoryByVerificationId(verificationId)).thenReturn(singleHistory);

        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status/history", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].fromStatus").value(VerificationStatus.PENDING.name()))
                .andExpect(jsonPath("$.data[0].toStatus").value(VerificationStatus.DOCUMENT_UPLOADED.name()))
                .andExpect(jsonPath("$.data[0].reason").value("Documents uploaded by customer"));

        verify(statusHistoryService).getStatusHistoryByVerificationId(verificationId);
    }

    @Test
    void testGetStatusHistory_CompleteWorkflow() throws Exception {

        List<StatusHistoryResponse> completeWorkflow = createCompleteWorkflowHistory();
        when(statusHistoryService.getStatusHistoryByVerificationId(verificationId)).thenReturn(completeWorkflow);

        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status/history", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data[0].fromStatus").value(VerificationStatus.PENDING.name()))
                .andExpect(jsonPath("$.data[4].toStatus").value(VerificationStatus.APPROVED.name()));

        verify(statusHistoryService).getStatusHistoryByVerificationId(verificationId);
    }

    @Test
    void testGetVerificationStatus_InvalidVerificationId() throws Exception {
        UUID invalidId = UUID.randomUUID();
        when(statusHistoryService.getLatestStatus(invalidId)).thenThrow(new RuntimeException("Verification not found"));

        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status", tenantId, invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(statusHistoryService).getLatestStatus(invalidId);
    }

    @Test
    void testGetStatusHistory_ServiceException() throws Exception {
        when(statusHistoryService.getStatusHistoryByVerificationId(verificationId))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(get("/tenants/{tenantId}/verification/{verificationId}/status/history", tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(statusHistoryService).getStatusHistoryByVerificationId(verificationId);
    }

    private List<StatusHistoryResponse> createCompleteWorkflowHistory() {
        List<StatusHistoryResponse> workflow = new ArrayList<>();

        workflow.add(createStatusHistoryResponse(VerificationStatus.PENDING, VerificationStatus.DOCUMENT_UPLOADED, 5));
        workflow.add(createStatusHistoryResponse(VerificationStatus.DOCUMENT_UPLOADED, VerificationStatus.IN_REVIEW, 4));
        workflow.add(createStatusHistoryResponse(VerificationStatus.IN_REVIEW, VerificationStatus.DOCUMENT_UPDATED, 3));
        workflow.add(createStatusHistoryResponse(VerificationStatus.DOCUMENT_UPDATED, VerificationStatus.IN_REVIEW, 2));
        workflow.add(createStatusHistoryResponse(VerificationStatus.IN_REVIEW, VerificationStatus.APPROVED, 1));

        return workflow;
    }

    private StatusHistoryResponse createStatusHistoryResponse(VerificationStatus from, VerificationStatus to, int daysAgo) {
        return StatusHistoryResponse.builder()
                .id(UUID.randomUUID())
                .verificationRequestId(verificationId)
                .fromStatus(from)
                .toStatus(to)
                .changedAt(LocalDateTime.now().minusDays(daysAgo))
                .changedBy(UUID.randomUUID())
                .reason("Status transition from " + from + " to " + to)
                .build();
    }
}