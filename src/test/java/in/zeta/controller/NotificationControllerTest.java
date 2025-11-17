package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.dto.response.NotificationResponse;
import in.zeta.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID notificationId = UUID.randomUUID();

    @Test
    void testGetUserNotifications_Success() throws Exception {
        List<NotificationResponse> notifications = Arrays.asList(
                NotificationResponse.builder()
                        .id(UUID.randomUUID())
                        .message("Test message 1")
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build(),
                NotificationResponse.builder()
                        .id(UUID.randomUUID())
                        .message("Test message 2")
                        .isRead(true)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(notificationService.getUserNotifications(userId)).thenReturn(notifications);

        mockMvc.perform(get("/tenants/{tenantId}/notifications/user/{userId}", tenantId, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Notification.FETCHED_NOTIFICATIONS_FOR_USER))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(notificationService).getUserNotifications(userId);
    }

    @Test
    void testGetUnreadCount_Success() throws Exception {
        Long unreadCount = 5L;
        when(notificationService.getUnreadCount(userId)).thenReturn(unreadCount);

        mockMvc.perform(get("/tenants/{tenantId}/notifications/unread/count/user/{userId}", tenantId, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Keys.NOTIFICATION_COUNT))
                .andExpect(jsonPath("$.data").value(5));

        verify(notificationService).getUnreadCount(userId);
    }

    @Test
    void testMarkAsRead_Success() throws Exception {
        mockMvc.perform(put("/tenants/{tenantId}/notifications/{notificationId}/user/{userId}/read",
                        tenantId, notificationId, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Notification.MARKED_AS_READ_SUCCESSFULLY))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(notificationService).markAsRead(notificationId, userId);
    }

    @Test
    void testGetUserNotifications_EmptyList() throws Exception {
        when(notificationService.getUserNotifications(userId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/tenants/{tenantId}/notifications/user/{userId}", tenantId, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(notificationService).getUserNotifications(userId);
    }
}