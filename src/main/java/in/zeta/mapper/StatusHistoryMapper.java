package in.zeta.mapper;

import in.zeta.entity.StatusHistory;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.requests.events.StatusUpdateEvent;
import in.zeta.dto.response.StatusHistoryResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public class StatusHistoryMapper {
    public static StatusHistoryResponse toStatusHistoryResponse(StatusHistory statusHistory) {
        if (statusHistory == null) return null;
        return StatusHistoryResponse.builder()
                .id(statusHistory.getId())
                .verificationRequestId(statusHistory.getVerificationRequest().getId())
                .fromStatus(statusHistory.getFromStatus())
                .toStatus(statusHistory.getToStatus())
                .changedBy(statusHistory.getChangedBy().getId())
                .reason(statusHistory.getReason())
                .changedAt(statusHistory.getChangedAt())
                .build();
    }

    public static StatusHistory toStatusHistory(StatusUpdateEvent event, VerificationRequest verificationRequest, Users user) {
        return StatusHistory.builder()
                .id(event.getId())
                .verificationRequest(verificationRequest)
                .fromStatus(event.getFromStatus())
                .toStatus(event.getToStatus())
                .changedBy(user)
                .reason(event.getReason())
                .build();
    }

    public static StatusUpdateEvent createEvent(UUID verificationRequestId, UUID changedBy, VerificationStatus fromStatus, VerificationStatus toStatus, String reason) {
        return StatusUpdateEvent.builder()
                .id(UUID.randomUUID())
                .verificationRequestId(verificationRequestId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .reason(reason)
                .changedAt(LocalDateTime.now())
                .build();
    }
    private StatusHistoryMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
}