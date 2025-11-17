package in.zeta.dto.requests.events;

import in.zeta.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreatedEvent {
    private UUID notificationId;
    private UUID userId;
    private UUID verificationRequestId;
    private NotificationType notificationType;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
