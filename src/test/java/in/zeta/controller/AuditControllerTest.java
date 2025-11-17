package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.dto.response.AuditLogsResponse;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import in.zeta.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID entityId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void testGetAuditTrail_Success() throws Exception {
        EntityType entityType = EntityType.VERIFICATION_REQUEST;
        List<AuditLogsResponse> auditLogs = Arrays.asList(
                AuditLogsResponse.builder()
                        .action(AuditAction.VERIFICATION_REQUEST_CREATED)
                        .entityType(entityType)
                        .entityId(entityId)
                        .userId(userId)
                        .timestamp(LocalDateTime.now())
                        .build(),
                AuditLogsResponse.builder()
                        .action(AuditAction.VERIFICATION_STATUS_CHANGED)
                        .entityType(entityType)
                        .entityId(entityId)
                        .userId(userId)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        when(auditService.getAuditTrail(entityType, entityId)).thenReturn(auditLogs);

        mockMvc.perform(get("/tenants/{tenantId}/audit/entityType/{entityType}/entityId/{entityId}",
                        tenantId, entityType, entityId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Audit.FETCHED_TRAIL_SUCCESSFULLY))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].entityType").value(entityType.name()))
                .andExpect(jsonPath("$.data[0].entityId").value(entityId.toString()));

        verify(auditService).getAuditTrail(entityType, entityId);
    }

    @Test
    void testGetAuditLogsByAction_Success() throws Exception {
        AuditAction action = AuditAction.VERIFICATION_REQUEST_CREATED;
        List<AuditLogsResponse> auditLogs = Arrays.asList(
                AuditLogsResponse.builder()
                        .id(UUID.randomUUID())
                        .action(action)
                        .entityType(EntityType.USER)
                        .entityId(UUID.randomUUID())
                        .userId(userId)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        when(auditService.getAuditLogsByAction(action)).thenReturn(auditLogs);

        mockMvc.perform(get("/tenants/{tenantId}/audit/action/{action}", tenantId, action)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Audit.FETCHED_TRAIL_SUCCESSFULLY))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].action").value(action.name()));

        verify(auditService).getAuditLogsByAction(action);
    }

    @Test
    void testGetAuditLogsByUser_Success() throws Exception {
        List<AuditLogsResponse> auditLogs = Arrays.asList(
                AuditLogsResponse.builder()
                        .id(UUID.randomUUID())
                        .action(AuditAction.VERIFICATION_STATUS_CHANGED)
                        .entityType(EntityType.COMMENT)
                        .entityId(UUID.randomUUID())
                        .userId(userId)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        when(auditService.getAuditLogsByUser(userId)).thenReturn(auditLogs);

        mockMvc.perform(get("/tenants/{tenantId}/audit/user/{userId}", tenantId, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Audit.FETCHED_LOGS_FOR_USER))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(userId.toString()));

        verify(auditService).getAuditLogsByUser(userId);
    }

    @Test
    void testGetAllAuditLogs_Success() throws Exception {
        List<AuditLogsResponse> auditLogs = Arrays.asList(
                AuditLogsResponse.builder()
                        .id(UUID.randomUUID())
                        .action(AuditAction.NOTIFICATION_READ)
                        .entityType(EntityType.NOTIFICATION)
                        .entityId(UUID.randomUUID())
                        .userId(userId)
                        .timestamp(LocalDateTime.now())
                        .build(),
                AuditLogsResponse.builder()
                        .id(UUID.randomUUID())
                        .action(AuditAction.COMMENT_DELETED)
                        .entityType(EntityType.USER)
                        .entityId(UUID.randomUUID())
                        .userId(UUID.randomUUID())
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        when(auditService.getAllAuditLogs()).thenReturn(auditLogs);

        mockMvc.perform(get("/tenants/{tenantId}/audit/all", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Audit.FETCHED_ALL_LOGS))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(auditService).getAllAuditLogs();
    }

    @Test
    void testGetAuditTrail_EmptyResult() throws Exception {
        EntityType entityType = EntityType.VERIFICATION_REQUEST;
        when(auditService.getAuditTrail(entityType, entityId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/tenants/{tenantId}/audit/entityType/{entityType}/entityId/{entityId}",
                        tenantId, entityType, entityId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(auditService).getAuditTrail(entityType, entityId);
    }
}