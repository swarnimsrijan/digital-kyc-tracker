package in.zeta.service;

import in.zeta.enums.VerificationStatus;
import in.zeta.dto.requests.events.StatusUpdateEvent;
import in.zeta.dto.response.StatusHistoryResponse;
import java.util.List;
import java.util.UUID;

public interface StatusHistoryService {
    StatusHistoryResponse getStatusHistoryById(UUID statusHistoryId);
    VerificationStatus getLatestStatus(UUID verificationRequestId);
    List<StatusHistoryResponse> getStatusHistoryByVerificationId(UUID verificationId);
    void updateStatusHistory(StatusUpdateEvent statusUpdateEvent);
    void publishStatusUpdateEvent(StatusUpdateEvent event);
    void updateStatusHistoryFromEvent(String event);
}
