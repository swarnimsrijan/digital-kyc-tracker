package in.zeta.mapper;

import in.zeta.entity.StatusHistory;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.requests.events.StatusUpdateEvent;
import in.zeta.dto.response.StatusHistoryResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StatusHistoryMapperTest {

    @Test
    void toStatusHistoryResponse_success() {
        Users user = Users.builder().id(UUID.randomUUID()).build();
        VerificationRequest vr = VerificationRequest.builder().id(UUID.randomUUID()).build();
        StatusHistory statusHistory = StatusHistory.builder()
                .id(UUID.randomUUID())
                .verificationRequest(vr)
                .fromStatus(VerificationStatus.PENDING)
                .toStatus(VerificationStatus.APPROVED)
                .changedBy(user)
                .reason("reason")
                .changedAt(LocalDateTime.now())
                .build();

        StatusHistoryResponse response = StatusHistoryMapper.toStatusHistoryResponse(statusHistory);

        assertNotNull(response);
        assertEquals(statusHistory.getId(), response.getId());
        assertEquals(vr.getId(), response.getVerificationRequestId());
        assertEquals(statusHistory.getFromStatus(), response.getFromStatus());
        assertEquals(statusHistory.getToStatus(), response.getToStatus());
        assertEquals(user.getId(), response.getChangedBy());
        assertEquals(statusHistory.getReason(), response.getReason());
        assertEquals(statusHistory.getChangedAt(), response.getChangedAt());
    }

    @Test
    void toStatusHistoryResponse_nullInput_returnsNull() {
        assertNull(StatusHistoryMapper.toStatusHistoryResponse(null));
    }

    @Test
    void toStatusHistory_success() {
        UUID id = UUID.randomUUID();
        Users user = Users.builder().id(UUID.randomUUID()).build();
        VerificationRequest vr = VerificationRequest.builder().id(UUID.randomUUID()).build();
        StatusUpdateEvent event = StatusUpdateEvent.builder()
                .id(id)
                .fromStatus(VerificationStatus.PENDING)
                .toStatus(VerificationStatus.APPROVED)
                .reason("reason")
                .build();

        StatusHistory statusHistory = StatusHistoryMapper.toStatusHistory(event, vr, user);

        assertNotNull(statusHistory);
        assertEquals(id, statusHistory.getId());
        assertEquals(vr, statusHistory.getVerificationRequest());
        assertEquals(VerificationStatus.PENDING, statusHistory.getFromStatus());
        assertEquals(VerificationStatus.APPROVED, statusHistory.getToStatus());
        assertEquals(user, statusHistory.getChangedBy());
        assertEquals("reason", statusHistory.getReason());
    }

    @Test
    void createEvent_success() {
        UUID vrId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        StatusUpdateEvent event = StatusHistoryMapper.createEvent(
                vrId, userId, VerificationStatus.PENDING, VerificationStatus.APPROVED, "reason"
        );

        assertNotNull(event);
        assertEquals(vrId, event.getVerificationRequestId());
        assertEquals(userId, event.getChangedBy());
        assertEquals(VerificationStatus.PENDING, event.getFromStatus());
        assertEquals(VerificationStatus.APPROVED, event.getToStatus());
        assertEquals("reason", event.getReason());
        assertNotNull(event.getChangedAt());
    }
}