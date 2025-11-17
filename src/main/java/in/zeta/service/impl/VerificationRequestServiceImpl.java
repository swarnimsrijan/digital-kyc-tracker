package in.zeta.service.impl;

import in.zeta.constants.Messages;
import in.zeta.entity.*;
import in.zeta.enums.*;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.exception.InvalidOperationException;
import in.zeta.exception.DataNotFoundException;
import in.zeta.exception.ResourceNotFoundException;
import in.zeta.mapper.VerificationRequestMapper;
import in.zeta.repository.*;
import in.zeta.dto.requests.CreateVerificationRequest;
import in.zeta.dto.requests.UpdateVerificationStatusRequest;
import in.zeta.dto.requests.events.StatusUpdateEvent;
import in.zeta.dto.response.CreateVerificationResponse;
import in.zeta.dto.response.VerificationRequestResponse;
import in.zeta.service.*;
import in.zeta.spectra.capture.SpectraLogger;
import jakarta.validation.Valid;
import olympus.trace.OlympusSpectra;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static in.zeta.mapper.AuditLogMapper.createAuditLogEvent;
import static in.zeta.mapper.NotificationMapper.createNotificationEvent;
import static in.zeta.mapper.StatusHistoryMapper.createEvent;
import static in.zeta.mapper.VerificationRequestMapper.*;

@Service
//@RequiredArgsConstructor
public class VerificationRequestServiceImpl implements VerificationRequestService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final UserService userService;
    private final StatusHistoryService statusHistoryService;
    private final VerificationRequestLimitService verificationRequestLimitService;

    public VerificationRequestServiceImpl(
            VerificationRequestRepository verificationRequestRepository,
            @Lazy NotificationService notificationService,
            AuditService auditService,
            UserService userService,
            @Lazy StatusHistoryService statusHistoryService,
            VerificationRequestLimitService verificationRequestLimitService
    ) {
        this.verificationRequestRepository = verificationRequestRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.userService = userService;
        this.statusHistoryService = statusHistoryService;
        this.verificationRequestLimitService = verificationRequestLimitService;
    }
    private static final SpectraLogger logger = OlympusSpectra.getLogger(VerificationRequestServiceImpl.class);

    @Override
    public CreateVerificationResponse createVerificationRequest(@Valid CreateVerificationRequest createVerificationRequest, UUID requestorId) {

        Users requestor = userService.getByIdOrThrow(requestorId, "Requestor not found " + requestorId);
        Users customer = userService.getByIdOrThrow(createVerificationRequest.getCustomerId(), "Customer not found " + createVerificationRequest.getCustomerId());

        boolean canCreate = verificationRequestLimitService.canCreateVerificationRequest(customer, requestor);

        if (!canCreate) {
            logger.error("Verification request limit exceeded")
                    .attr("customerId", customer.getId())
                    .attr("requestorId", requestor.getId())
                    .log();
            throw new InvalidOperationException("Verification request limit exceeded for this customer-requestor combination");
        }

        logger.info("Creating verification request")
                .attr("customerId", customer.getId())
                .attr("requestorId", requestor.getId())
                .log();

        VerificationRequest verificationRequest = createrequest(
                customer,
                requestor,
                createVerificationRequest.getRequestReason()
        );

        VerificationRequest saved = verificationRequestRepository.save(verificationRequest);

        try {
            verificationRequestLimitService.incrementRequestCount(customer, requestor);

            logger.info("Request count incremented successfully")
                    .attr("customerId", customer.getId())
                    .attr("requestorId", requestor.getId())
                    .attr("verificationRequestId", saved.getId())
                    .log();
        } catch (Exception e) {
            logger.error("Failed to increment request count")
                    .attr("customerId", customer.getId())
                    .attr("requestorId", requestor.getId())
                    .attr("verificationRequestId", saved.getId())
                    .attr("error", e.getMessage())
                    .log();
        }

        StatusUpdateEvent statusUpdateEvent = createEvent(
                saved.getId(),
                requestorId,
                null,
                saved.getStatus(),
                saved.getRequestReason()
        );

        statusHistoryService.publishStatusUpdateEvent(statusUpdateEvent);

        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.VERIFICATION_REQUEST,
                verificationRequest.getId(),
                AuditAction.VERIFICATION_REQUEST_CREATED,
                requestor,
                String.format(""),
                String.format("Verification request Created successfully: %s", verificationRequest.getId())
        );

        auditService.publishAuditLogEvent(event);

        return convertToCreateResponse(saved);
    }

    @Override
    public List<VerificationRequestResponse> getRequestorVerifications(UUID requestorId) {
        Users user = userService.getByIdOrThrow(requestorId, "Requestor not found with ID: " + requestorId);
        if(user.getRole() != Role.VERIFICATION_REQUESTOR) {
            throw new InvalidOperationException("User with ID: " + requestorId + " is not a authorized");
        }
        List<VerificationRequest> requests = verificationRequestRepository.findByRequestorId(requestorId);
        return requests.stream()
                .map(VerificationRequestMapper::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VerificationRequestResponse getVerificationRequest(UUID verificationId) {
        VerificationRequest verificationRequest = getByIdOrThrow(verificationId);
        return convertToResponse(verificationRequest);
    }

    @Override
    public List<VerificationRequestResponse> getCustomerVerifications(UUID customerId) {
        List<VerificationRequest> requests = verificationRequestRepository.findByCustomerId(customerId);
        return requests.stream()
                .map(VerificationRequestMapper::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VerificationRequestResponse> getOfficerVerifications(UUID officerId) {
        List<VerificationRequest> requests = verificationRequestRepository.findByAssignedOfficerId(officerId);
        return requests.stream()
                .map(VerificationRequestMapper::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VerificationRequestResponse> getOpenVerifications() {
        List<VerificationRequest> requests = verificationRequestRepository.findByStatus(VerificationStatus.PENDING);
        return requests.stream()
                .map(VerificationRequestMapper::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VerificationRequestResponse updateStatus(UUID verificationId, @Valid UpdateVerificationStatusRequest updateVerificationStatusRequest, UUID officerId) {
        VerificationRequest verificationRequest = getByIdOrThrow(verificationId);

        if (verificationRequest.getAssignedOfficer() == null) {
            throw new RuntimeException("Cannot update status. No officer assigned to this request.");
        }

        if(!verificationRequest.getAssignedOfficer().getId().equals(officerId)) {
            throw new RuntimeException("Cannot update status. Officer not assigned to this request.");
        }

        VerificationStatus currentStatus = verificationRequest.getStatus();
        verificationRequest.setStatus(updateVerificationStatusRequest.getStatus());
        verificationRequest.setUpdatedAt(LocalDateTime.now());

        if (updateVerificationStatusRequest.getStatus() == VerificationStatus.APPROVED) {
            verificationRequest.setApprovedAt(LocalDateTime.now());
        } else if (updateVerificationStatusRequest.getStatus() == VerificationStatus.REJECTED) {
            verificationRequest.setRejectedAt(LocalDateTime.now());
        }

        VerificationRequest updated = verificationRequestRepository.save(verificationRequest);

        StatusUpdateEvent statusUpdateEvent = createEvent(
                verificationId,
                officerId,
                currentStatus,
                updated.getStatus(),
                updateVerificationStatusRequest.getReason()
        );

        statusHistoryService.publishStatusUpdateEvent(statusUpdateEvent);

        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.VERIFICATION_REQUEST,
                verificationRequest.getId(),
                AuditAction.VERIFICATION_STATUS_CHANGED,
                verificationRequest.getAssignedOfficer(),
                String.format("Old verification request status, %s", currentStatus),
                String.format("Updated Verification request status, ", updated.getStatus())
        );

        auditService.publishAuditLogEvent(event);

        return convertToResponse(updated);
    }

    @Override
    public VerificationRequestResponse assignToOfficer(UUID verificationId, UUID officerId) {
        VerificationRequest verificationRequest = getByIdOrThrow(verificationId);
        Users officer = userService.getByIdOrThrow(officerId, "Officer not found with ID: " + officerId);
        Users currentOfficer = verificationRequest.getAssignedOfficer();
        verificationRequest.setAssignedOfficer(officer);
        verificationRequest.setUpdatedAt(LocalDateTime.now());
        VerificationRequest updated = verificationRequestRepository.save(verificationRequest);

        logger.info("Creating notification event for officer updation")
                .attr("officerId", officerId)
                .attr("verificationId", verificationId)
                .log();

        NotificationCreatedEvent notificationCreatedEvent = createNotificationEvent(
                officerId,
                verificationId,
                NotificationType.ASSIGNED_TO_OFFICER,
                Messages.Notification.ASSIGNED_TO_OFFICER
        );
        notificationService.publishNotificationEvent(notificationCreatedEvent);

        logger.info("Created notification event for officer updation")
                .attr("officerId", officerId)
                .attr("verificationId", verificationId)
                .log();

        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.VERIFICATION_REQUEST,
                verificationRequest.getId(),
                AuditAction.VERIFICATION_REQUEST_REASSIGNED,
                verificationRequest.getRequestor(),
                String.format("Old assigned officer: %s", currentOfficer.getId()),
                String.format("New assigned officer: %s", updated.getAssignedOfficer().getId())
        );
        auditService.publishAuditLogEvent(event);

        return convertToResponse(updated);
    }

    @Override
    public List<VerificationRequestResponse> getPendingRequestsAssignedToOfficer(UUID officerId) {
        List<VerificationRequest> requests = verificationRequestRepository.findByAssignedOfficerId(officerId).stream()
                .filter(req -> req.getStatus() == VerificationStatus.PENDING)
                .collect(Collectors.toList());
        return requests.stream()
                .map(VerificationRequestMapper::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VerificationStatus getVerificationStatus(UUID verificationId) {
        VerificationRequest verificationRequest = getByIdOrThrow(verificationId);
        return verificationRequest.getStatus();
    }

    @Override
    public Users getUserByVerificationRequest(UUID verificationId) {
        VerificationRequest verificationRequest = getByIdOrThrow(verificationId);
        return verificationRequest.getCustomer();
    }

    @Override
    public VerificationRequest getByIdOrThrow(UUID verificationId) {
        return verificationRequestRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Verification Request not found with ID: " + verificationId));
    }

    @Override
    public void saveVerificationRequest(VerificationRequest verificationRequest) {
        verificationRequestRepository.save(verificationRequest);
    }

    @Override
    public List<VerificationRequest> findByAssignedOfficerAndStatus(Users officer, VerificationStatus status){
        return verificationRequestRepository.findByAssignedOfficerAndStatus(officer, status);
    }

    @Override
    public void save(VerificationRequest verificationRequest) {
        verificationRequestRepository.save(verificationRequest);
    }
}