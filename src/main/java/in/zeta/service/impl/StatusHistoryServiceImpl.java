package in.zeta.service.impl;

import in.zeta.constants.Messages;
import in.zeta.entity.StatusHistory;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import in.zeta.enums.NotificationType;
import in.zeta.enums.VerificationStatus;
import in.zeta.exception.DataNotFoundException;
import in.zeta.producer.EventProducer;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.dto.requests.events.StatusUpdateEvent;
import in.zeta.dto.response.StatusHistoryResponse;
import in.zeta.service.*;
import in.zeta.repository.StatusHistoryRepository;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static in.zeta.mapper.AuditLogMapper.createAuditLogEvent;
import static in.zeta.mapper.NotificationMapper.createNotificationEvent;
import static in.zeta.mapper.StatusHistoryMapper.toStatusHistory;
import static in.zeta.mapper.StatusHistoryMapper.toStatusHistoryResponse;

@Service
@RequiredArgsConstructor
public class StatusHistoryServiceImpl implements StatusHistoryService {

    @Value("${atropos.status.topic}")
    private String statusUpdateTopic;

    private final StatusHistoryRepository statusHistoryRepository;
    private final VerificationRequestService verificationRequestService;
    private final UserService userService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final EventProducer eventProducer;
    private final SpectraLogger logger = OlympusSpectra.getLogger(StatusHistoryServiceImpl.class);

    @Override
    public StatusHistoryResponse getStatusHistoryById(UUID statusHistoryId) {
        try {
            logger.info("Fetching status history by ID")
                    .attr("statusHistoryId", statusHistoryId.toString())
                    .log();

            StatusHistory statusHistory = statusHistoryRepository.findById(statusHistoryId)
                    .orElseThrow(() -> new DataNotFoundException("status_history not found", "id", "" + statusHistoryId));

            logger.info("Successfully retrieved status history")
                    .attr("statusHistoryId", statusHistoryId.toString())
                    .log();

            return toStatusHistoryResponse(statusHistory);
        } catch (DataNotFoundException e) {
            logger.error("Status history not found")
                    .attr("statusHistoryId", statusHistoryId.toString())
                    .attr("error", e.getMessage())
                    .log();
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching status history by ID")
                    .attr("statusHistoryId", statusHistoryId.toString())
                    .attr("error", e.getMessage())
                    .log();
            throw new RuntimeException("Failed to fetch status history", e);
        }
    }

    @Override
    public VerificationStatus getLatestStatus(UUID verificationRequestId) {
        try {
            logger.info("Fetching latest status for verification request")
                    .attr("verificationRequestId", verificationRequestId.toString())
                    .log();

            VerificationRequest verificationRequest = verificationRequestService.getByIdOrThrow(verificationRequestId);
            StatusHistory statusHistory = statusHistoryRepository
                    .findLatestStatusHistory(verificationRequest)
                    .orElseThrow(() -> new DataNotFoundException("status_history not found", "verification_request_id", verificationRequestId));

            return statusHistory.getToStatus();
        } catch (DataNotFoundException e) {
            logger.error("Latest status not found")
                    .attr("tableName", e.getTableName())
                    .attr("fieldName", e.getFieldName())
                    .attr("fieldValue", e.getFieldValue().toString())
                    .log();
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching latest status")
                    .attr("verificationRequestId", verificationRequestId.toString())
                    .attr("error", e.getMessage())
                    .log();
            throw new RuntimeException("Failed to fetch latest status", e);
        }
    }

    @Override
    public List<StatusHistoryResponse> getStatusHistoryByVerificationId(UUID verificationRequestId) {
        try {
            logger.info("Fetching status history for verification request")
                    .attr("verificationRequestId", verificationRequestId.toString())
                    .log();

            VerificationRequest verificationRequest = verificationRequestService.getByIdOrThrow(verificationRequestId);
            List<StatusHistory> historyList = statusHistoryRepository.findByVerificationRequestOrderByChangedAtDesc(verificationRequest);

            if (historyList.isEmpty()) {
                throw new DataNotFoundException("status_history not found", "verification_request_id", "No status history found for the given verification request ID");
            }

            logger.info("Successfully retrieved status history")
                    .attr("verificationRequestId", verificationRequestId.toString())
                    .attr("recordCount", String.valueOf(historyList.size()))
                    .log();

            return historyList.stream()
                    .map(statusHistory -> toStatusHistoryResponse(statusHistory))
                    .collect(Collectors.toList());
        } catch (DataNotFoundException e) {
            logger.error("Verification request not found")
                    .attr("verificationRequestId", verificationRequestId.toString())
                    .attr("error", e.getMessage())
                    .log();
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching status history for verification request")
                    .attr("verificationRequestId", verificationRequestId.toString())
                    .attr("error", e.getMessage())
                    .log();
            throw new RuntimeException("Failed to fetch status history", e);
        }
    }

    @Override
    public void publishStatusUpdateEvent(StatusUpdateEvent event) {
        eventProducer.publishEvent(
                EntityType.STATUS_HISTORY.toString(),
                event.getId().toString(),
                statusUpdateTopic,
                event
        );
    }

    @Override
    public void updateStatusHistory(StatusUpdateEvent statusUpdateEvent) {
        VerificationRequest verificationRequest = verificationRequestService.getByIdOrThrow(statusUpdateEvent.getVerificationRequestId());
        Users user = userService.getByIdOrThrow(statusUpdateEvent.getChangedBy(), "User not found with ID: " + statusUpdateEvent.getChangedBy());

        StatusHistory statusHistory = toStatusHistory(statusUpdateEvent, verificationRequest, user);
        StatusHistory savedStatusHistory = statusHistoryRepository.save(statusHistory);

        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.STATUS_HISTORY,
                savedStatusHistory.getId(),
                AuditAction.STATUS_HISTORY_UPDATED,
                user,
                statusUpdateEvent.getFromStatus().toString(),
                String.format("Status updated from %s to %s", statusUpdateEvent.getFromStatus(), statusUpdateEvent.getToStatus())
        );

        auditService.publishAuditLogEvent(event);

        NotificationType status = null;
        String message = "";
        UUID customerId = null;

        switch (statusUpdateEvent.getToStatus()) {
            case PENDING:
                verificationRequest.setUpdatedAt(savedStatusHistory.getChangedAt());
                message = Messages.Notification.VERIFICATION_REQUESTED;
                status = NotificationType.VERIFICATION_REQUESTED;
                customerId = verificationRequest.getCustomer().getId();
                break;
            case DOCUMENT_UPDATED:
                verificationRequest.setUpdatedAt(savedStatusHistory.getChangedAt());
                message = Messages.Notification.DOCUMENT_UPDATED;
                status = NotificationType.DOCUMENT_UPDATED;
                customerId = verificationRequest.getAssignedOfficer().getId();
                sendNotificationToRequestorIfNeeded(status, message, verificationRequest);
                break;
            case DOCUMENT_UPLOADED:
                verificationRequest.setUpdatedAt(savedStatusHistory.getChangedAt());
                message = Messages.Notification.DOCUMENT_UPLOADED;
                status = NotificationType.DOCUMENT_UPLOADED;
                customerId = verificationRequest.getAssignedOfficer().getId();
                sendNotificationToRequestorIfNeeded(status, message, verificationRequest);
                break;
            case APPROVED:
                verificationRequest.setApprovedAt(savedStatusHistory.getChangedAt());
                message = Messages.Notification.VERIFICATION_APPROVED;
                status = NotificationType.VERIFICATION_APPROVED;
                customerId = verificationRequest.getCustomer().getId();
                break;
            case REJECTED:
                verificationRequest.setRejectedAt(savedStatusHistory.getChangedAt());
                message = Messages.Notification.VERIFICATION_REJECTED;
                status = NotificationType.VERIFICATION_REJECTED;
                customerId = verificationRequest.getCustomer().getId();
                break;
            case SENT_BACK:
                verificationRequest.setUpdatedAt(savedStatusHistory.getChangedAt());
                message = Messages.Notification.SENT_BACK_FOR_DETAILS;
                status = NotificationType.SENT_BACK_FOR_DETAILS;
                customerId = verificationRequest.getCustomer().getId();
                break;
            case IN_REVIEW:
                verificationRequest.setUpdatedAt(savedStatusHistory.getChangedAt());
                message = Messages.Notification.ASSIGNED_TO_OFFICER;
                status = NotificationType.ASSIGNED_TO_OFFICER;
                customerId = verificationRequest.getCustomer().getId();
                break;
            default:
                break;
        }

        NotificationCreatedEvent customerNotification = createNotificationEvent(
                customerId,
                verificationRequest.getId(),
                status,
                message
        );
        notificationService.publishNotificationEvent(customerNotification);
    }

    @Override
    public void updateStatusHistoryFromEvent(String event){
        try {
            StatusUpdateEvent statusUpdateEvent = JsonUtil.parseStatusUpdateEvent(event);
            updateStatusHistory(statusUpdateEvent);
        } catch (Exception e) {

            throw new RuntimeException("Failed to process status update event", e);
        }

    }
    private void sendNotificationToRequestorIfNeeded(NotificationType status, String message, VerificationRequest request) {
        logger.info("Checking if notification to requestor is needed")
                .attr("verificationRequestId", request.getId().toString())
                .attr("status", status.toString())
                .log();

            NotificationCreatedEvent requestorNotification = createNotificationEvent(
                    request.getRequestor().getId(),
                    request.getId(),
                    status,
                    message
            );

            notificationService.publishNotificationEvent(requestorNotification);
    }

}