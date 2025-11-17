package in.zeta.service.impl;

import in.zeta.constants.Messages;
import in.zeta.entity.Document;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.*;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.service.*;
import in.zeta.spectra.capture.SpectraLogger;
import olympus.trace.OlympusSpectra;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static in.zeta.mapper.AuditLogMapper.createAuditLogEvent;
import static in.zeta.mapper.NotificationMapper.createNotificationEvent;

@Service
@Transactional
public class OfficerAssignemetServiceImpl implements OfficerAssignmentService {

    public static final SpectraLogger logger = OlympusSpectra.getLogger(OfficerAssignemetServiceImpl.class);
    private final AuditService auditService;
    private final VerificationRequestService verificationRequestService;
    private final DocumentService documentService;
    private final UserService userService;
    private final NotificationService notificationService;

    public OfficerAssignemetServiceImpl(AuditService auditService,
                                       VerificationRequestService verificationRequestService,
                                       @Lazy DocumentService documentService,
                                       UserService userService,
                                       NotificationService notificationService) {
        this.auditService = auditService;
        this.verificationRequestService = verificationRequestService;
        this.documentService = documentService;
        this.userService = userService;
        this.notificationService = notificationService;
    }


    @Override
    public BigDecimal getOfficerWorkload(UUID officerId) {
        Users officer = userService.getByIdOrThrow(officerId, "Officer not found " + officerId) ;

        List<VerificationRequest> activeRequests = verificationRequestService
                .findByAssignedOfficerAndStatus(officer, VerificationStatus.DOCUMENT_UPLOADED);

        BigDecimal requestCount = BigDecimal.valueOf(activeRequests.size());
        BigDecimal totalDocuments = BigDecimal.ZERO;
        BigDecimal totalDocumentSize = BigDecimal.ZERO;

        for (VerificationRequest request : activeRequests) {

            List<Document> documents = documentService.findByVerificationRequestId(request.getId());
            totalDocuments = totalDocuments.add(BigDecimal.valueOf(documents.size()));

            BigDecimal documentSizeSum = documents.stream()
                    .map(Document::getFileSize)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalDocumentSize = totalDocumentSize.add(documentSizeSum);
        }

        BigDecimal sizeInMB = totalDocumentSize.divide(BigDecimal.valueOf(1024L * 1024), 0, RoundingMode.DOWN);

        BigDecimal workloadScore = requestCount.multiply(BigDecimal.valueOf(10))
                .add(totalDocuments.multiply(BigDecimal.valueOf(2)))
                .add(sizeInMB);

        return workloadScore;
    }

    @Override
    @Transactional
    public void assignOfficerToVerification(UUID verificationId) {
        VerificationRequest verificationRequest = verificationRequestService.getByIdOrThrow(verificationId);
        List<Users> availableOfficers = userService.findByRole(Role.VERIFICATION_OFFICER);

        if (availableOfficers.isEmpty()) {
            throw new RuntimeException("No verification officers available");
        }

        Users selectedOfficer = availableOfficers.stream()
                .min(Comparator.comparing(officer -> getOfficerWorkload(officer.getId())))
                .orElseThrow(() -> new RuntimeException("Unable to assign officer"));

        verificationRequest.setAssignedOfficer(selectedOfficer);
        verificationRequest.setStatus(VerificationStatus.IN_REVIEW);
        verificationRequestService.saveVerificationRequest(verificationRequest);

        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.VERIFICATION_REQUEST,
                verificationRequest.getId(),
                AuditAction.VERIFICATION_REQUEST_ASSIGNED,
                selectedOfficer,
                String.format("Officer not yet assigned"),
                String.format("Officer assigned from verification: %s, to role: %s",
                        verificationId, selectedOfficer.getId())
        );

        logger.info("Creating audit log event for officer assignment")
                .attr("verificationRequestId", verificationRequest.getId())
                .attr("assignedOfficerId", selectedOfficer.getId())
                .log();

        auditService.publishAuditLogEvent(event);

        logger.info("Published audit log event for officer assignment")
                .attr("verificationRequestId", verificationRequest.getId())
                .attr("assignedOfficerId", selectedOfficer.getId())
                .log();

        logger.info("Notification event created for assigned officer")
                .attr("verificationRequestId", verificationRequest.getId())
                .attr("assignedOfficerId", selectedOfficer.getId())
                .log();

        NotificationCreatedEvent notificationCreatedEvent = createNotificationEvent(
                selectedOfficer.getId(),
                verificationRequest.getId(),
                NotificationType.ASSIGNED_TO_OFFICER,
                Messages.Notification.ASSIGNED_TO_OFFICER
        );

        logger.info("Publishing notification event for assigned officer")
                .attr("verificationRequestId", verificationRequest.getId())
                .attr("assignedOfficerId", selectedOfficer.getId())
                .log();

        notificationService.publishNotificationEvent(notificationCreatedEvent);
    }


}
