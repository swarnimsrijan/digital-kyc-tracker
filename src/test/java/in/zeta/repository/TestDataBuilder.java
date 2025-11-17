package in.zeta.repository;

import in.zeta.entity.*;
import in.zeta.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static in.zeta.enums.DocumentType.PASSPORT;


public class TestDataBuilder {

    // Users
    public static Users createCustomer() {
        Users customer = new Users();
        // Don't set ID - let JPA generate it
        customer.setUsername("customer123");
        customer.setEmail("customer@test.com");
        customer.setPassword("hashedPassword123");
        customer.setRole(Role.CUSTOMER);
        customer.setCreatedAt(LocalDateTime.now().minusDays(30));
        return customer;
    }

    public static Users createRequestor() {
        Users requestor = new Users();
        requestor.setUsername("requestor123");
        requestor.setEmail("requestor@test.com");
        requestor.setPassword("hashedPassword456");
        requestor.setRole(Role.VERIFICATION_REQUESTOR);
        requestor.setCreatedAt(LocalDateTime.now().minusDays(20));
        return requestor;
    }

    public static Users createOfficer() {
        Users officer = new Users();
        officer.setUsername("officer123");
        officer.setEmail("officer@test.com");
        officer.setPassword("hashedPassword789");
        officer.setRole(Role.VERIFICATION_OFFICER);
        officer.setCreatedAt(LocalDateTime.now().minusDays(25));
        return officer;
    }

    public static Users createCustomerWithUsername(String username, String email) {
        Users customer = createCustomer();
        customer.setUsername(username);
        customer.setEmail(email);
        return customer;
    }

    public static Users createRequestorWithUsername(String username, String email) {
        Users requestor = createRequestor();
        requestor.setUsername(username);
        requestor.setEmail(email);
        return requestor;
    }

    public static Users createOfficerWithUsername(String username, String email) {
        Users officer = createOfficer();
        officer.setUsername(username);
        officer.setEmail(email);
        return officer;
    }

    // Verification Request
    public static VerificationRequest createVerificationRequest(Users customer, Users requestor, Users officer) {
        VerificationRequest vr = new VerificationRequest();
        vr.setCustomer(customer);
        vr.setRequestor(requestor);
        vr.setAssignedOfficer(officer);
        vr.setStatus(VerificationStatus.PENDING);
        vr.setRequestReason("Background verification for employment");
        vr.setCreatedAt(LocalDateTime.now().minusDays(5));
        return vr;
    }

    public static VerificationRequest createVerificationRequestWithStatus(
            Users customer, Users requestor, Users officer, VerificationStatus status) {
        VerificationRequest vr = createVerificationRequest(customer, requestor, officer);
        vr.setStatus(status);
        return vr;
    }

    // Document
    public static Document createDocument(VerificationRequest vr, Users uploadedBy) {
        Document doc = new Document();
        doc.setVerificationRequest(vr);
        doc.setUploadedBy(uploadedBy);
        doc.setDocumentType(PASSPORT);
        doc.setFileName("passport.pdf");
        doc.setFileSize(new BigDecimal(2048.50));
        doc.setContentType("application/pdf");
        doc.setFileHash("abc123hash456def" + System.nanoTime()); // Make unique
        doc.setIsActive(true);
        doc.setDescription("Passport copy");
        doc.setUploadedAt(LocalDateTime.now().minusDays(3));
        return doc;
    }

    public static Document createDocumentWithHash(VerificationRequest vr, Users uploadedBy, String fileHash) {
        Document doc = createDocument(vr, uploadedBy);
        doc.setFileHash(fileHash);
        return doc;
    }

    public static Document createInactiveDocument(VerificationRequest vr, Users uploadedBy) {
        Document doc = createDocument(vr, uploadedBy);
        doc.setIsActive(false);
        doc.setFileHash("inactive123hash" + System.nanoTime());
        return doc;
    }

    // Comment
    public static Comment createComment(VerificationRequest vr, Users createdBy) {
        Comment comment = new Comment();
        comment.setVerificationRequest(vr);
        comment.setCreatedBy(createdBy);
        comment.setCommentText("This is a test comment");
        comment.setCommentType(CommentType.GENERAL);
        comment.setCreatedAt(LocalDateTime.now().minusDays(2));
        return comment;
    }

    public static Comment createCommentWithText(VerificationRequest vr, Users createdBy, String text) {
        Comment comment = createComment(vr, createdBy);
        comment.setCommentText(text);
        return comment;
    }

    // Status History
    public static StatusHistory createStatusHistory(VerificationRequest vr, Users changedBy) {
        StatusHistory history = new StatusHistory();
        history.setVerificationRequest(vr);
        history.setChangedBy(changedBy);
        history.setFromStatus(VerificationStatus.PENDING);
        history.setToStatus(VerificationStatus.IN_REVIEW);
        history.setReason("Starting verification process");
        history.setChangedAt(LocalDateTime.now().minusDays(1));
        return history;
    }

    public static StatusHistory createStatusHistoryWithStatus(
            VerificationRequest vr, Users changedBy, VerificationStatus fromStatus, VerificationStatus toStatus) {
        StatusHistory history = createStatusHistory(vr, changedBy);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        return history;
    }

    // Notification
    public static Notification createNotification(Users user, VerificationRequest vr) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUser(user);
        notification.setVerificationRequest(vr);
        notification.setNotificationType(NotificationType.VERIFICATION_REQUESTED);
        notification.setMessage("Your verification request status has been created");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now().minusHours(2));
        notification.setSentAt(LocalDateTime.now().minusHours(2));
        return notification;
    }

    public static Notification createReadNotification(Users user, VerificationRequest vr) {
        Notification notification = createNotification(user, vr);
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now().minusHours(1));
        return notification;
    }

    public static Notification createNotificationWithMessage(Users user, VerificationRequest vr, String message) {
        Notification notification = createNotification(user, vr);
        notification.setMessage(message);
        return notification;
    }

    // Verification Request Limit
    public static VerificationRequestLimit createVerificationLimit(Users customer, Users requestor, int year) {
        VerificationRequestLimit limit = new VerificationRequestLimit();
        limit.setCustomer(customer);
        limit.setVerificationRequestor(requestor);
        limit.setYear(year);
        limit.setRequestCount(5);
        limit.setTotalRequests(5);
        limit.setMaxAllowedRequests(10);
        limit.setCreatedAt(LocalDateTime.now().minusDays(10));
        return limit;
    }

    public static VerificationRequestLimit createVerificationLimitWithCounts(
            Users customer, Users requestor, int year, int requestCount, int maxAllowed) {
        VerificationRequestLimit limit = createVerificationLimit(customer, requestor, year);
        limit.setRequestCount(requestCount);
        limit.setTotalRequests(requestCount);
        limit.setMaxAllowedRequests(maxAllowed);
        return limit;
    }

    // Audit Logs
    public static AuditLogs createAuditLog(Users user, EntityType entityType, UUID entityId, AuditAction action) {
        AuditLogs auditLog = new AuditLogs();
        auditLog.setUser(user);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setOldValue("old_value");
        auditLog.setNewValue("new_value");
        auditLog.setTimestamp(LocalDateTime.now().minusHours(1));
        return auditLog;
    }

    public static AuditLogs createAuditLogWithValues(
            Users user, EntityType entityType, UUID entityId, AuditAction action,
            String oldValue, String newValue) {
        AuditLogs auditLog = createAuditLog(user, entityType, entityId, action);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        return auditLog;
    }


}