package in.zeta.repository;

import in.zeta.entity.AuditLogs;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuditLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private Users customer;
    private Users officer;
    private VerificationRequest verificationRequest;
    private UUID testEntityId;

    @BeforeEach
    void setUp() {
        customer = TestDataBuilder.createCustomer();
        Users requestor = TestDataBuilder.createRequestor();
        officer = TestDataBuilder.createOfficer();

        entityManager.persist(customer);
        entityManager.persist(requestor);
        entityManager.persist(officer);

        verificationRequest = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        entityManager.persist(verificationRequest);
        entityManager.flush();

        testEntityId = verificationRequest.getId();
    }

    @Test
    void testFindByEntityTypeAndEntityIdOrderByTimestampDesc() {
        // Given
        AuditLogs log1 = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_CREATED);
        log1.setTimestamp(LocalDateTime.now().minusDays(3));

        AuditLogs log2 = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_ASSIGNED);
        log2.setTimestamp(LocalDateTime.now().minusDays(2));

        AuditLogs log3 = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_STATUS_CHANGED);
        log3.setTimestamp(LocalDateTime.now().minusDays(1));

        // Different entity - should not be returned
        AuditLogs differentEntity = TestDataBuilder.createAuditLog(
                officer, EntityType.DOCUMENT, UUID.randomUUID(), AuditAction.DOCUMENT_UPLOADED);

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(log3);
        entityManager.persist(differentEntity);
        entityManager.flush();

        // When
        List<AuditLogs> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                EntityType.VERIFICATION_REQUEST, testEntityId);

        // Then
        assertThat(logs).hasSize(3);
        // Verify descending order by timestamp
        assertThat(logs.get(0).getAction()).isEqualTo(AuditAction.VERIFICATION_STATUS_CHANGED);
        assertThat(logs.get(2).getAction()).isEqualTo(AuditAction.VERIFICATION_REQUEST_CREATED);
        assertThat(logs.get(0).getTimestamp()).isAfter(logs.get(1).getTimestamp());
    }

    @Test
    void testFindByActionOrderByTimestampDesc() {
        // Given
        AuditLogs docLog1 = TestDataBuilder.createAuditLog(
                officer, EntityType.DOCUMENT, UUID.randomUUID(), AuditAction.DOCUMENT_UPLOADED);
        docLog1.setTimestamp(LocalDateTime.now().minusDays(3));

        AuditLogs docLog2 = TestDataBuilder.createAuditLog(
                officer, EntityType.DOCUMENT, UUID.randomUUID(), AuditAction.DOCUMENT_UPLOADED);
        docLog2.setTimestamp(LocalDateTime.now().minusDays(1));

        AuditLogs updateLog = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_STATUS_CHANGED);
        updateLog.setTimestamp(LocalDateTime.now().minusDays(2));

        entityManager.persist(docLog1);
        entityManager.persist(docLog2);
        entityManager.persist(updateLog);
        entityManager.flush();

        // When
        List<AuditLogs> createLogs = auditLogRepository.findByActionOrderByTimestampDesc(AuditAction.DOCUMENT_UPLOADED);

        assertThat(createLogs).hasSize(2);
        assertThat(createLogs).allMatch(log -> log.getAction() == AuditAction.DOCUMENT_UPLOADED);
        // Verify descending order
        assertThat(createLogs.get(0).getTimestamp()).isAfter(createLogs.get(1).getTimestamp());
    }

    @Test
    void testFindByUserIdOrderByTimestampDesc() {
        // Given
        AuditLogs log1 = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_CREATED);
        log1.setTimestamp(LocalDateTime.now().minusDays(3));

        AuditLogs log2 = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_STATUS_CHANGED);
        log2.setTimestamp(LocalDateTime.now().minusDays(1));

        AuditLogs customerLog = TestDataBuilder.createAuditLog(
                customer, EntityType.DOCUMENT, UUID.randomUUID(), AuditAction.DOCUMENT_UPLOADED);

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(customerLog);
        entityManager.flush();

        // When
        List<AuditLogs> officerLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(officer.getId());

        // Then
        assertThat(officerLogs).hasSize(2);
        assertThat(officerLogs).allMatch(log -> log.getUser().getId().equals(officer.getId()));
        // Verify descending order
        assertThat(officerLogs.get(0).getTimestamp()).isAfter(officerLogs.get(1).getTimestamp());
    }

    @Test
    void testFindAllByOrderByTimestampDesc() {
        // Given
        AuditLogs log1 = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_CREATED);
        log1.setTimestamp(LocalDateTime.now().minusDays(5));

        AuditLogs log2 = TestDataBuilder.createAuditLog(
                customer, EntityType.DOCUMENT, UUID.randomUUID(), AuditAction.DOCUMENT_UPLOADED);
        log2.setTimestamp(LocalDateTime.now().minusDays(3));

        AuditLogs log3 = TestDataBuilder.createAuditLog(
                officer, EntityType.COMMENT, UUID.randomUUID(), AuditAction.COMMENT_DELETED);
        log3.setTimestamp(LocalDateTime.now().minusDays(1));

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(log3);
        entityManager.flush();

        // When
        List<AuditLogs> allLogs = auditLogRepository.findAllByOrderByTimestampDesc();

        // Then
        assertThat(allLogs).hasSize(3);
        // Verify descending order
        assertThat(allLogs.get(0).getTimestamp()).isAfter(allLogs.get(1).getTimestamp());
        assertThat(allLogs.get(1).getTimestamp()).isAfter(allLogs.get(2).getTimestamp());
        assertThat(allLogs.get(0).getAction()).isEqualTo(AuditAction.COMMENT_DELETED);
    }

    @Test
    void testFindByEntityTypeAndEntityIdOrderByTimestampDesc_NoResults() {
        // When
        List<AuditLogs> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                EntityType.VERIFICATION_REQUEST, UUID.randomUUID());

        // Then
        assertThat(logs).isEmpty();
    }

    @Test
    void testFindByActionOrderByTimestampDesc_NoResults() {
        // When
        List<AuditLogs> logs = auditLogRepository.findByActionOrderByTimestampDesc(AuditAction.COMMENT_DELETED);

        // Then
        assertThat(logs).isEmpty();
    }

    @Test
    void testFindByUserIdOrderByTimestampDesc_NoResults() {
        // When
        List<AuditLogs> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(UUID.randomUUID());

        // Then
        assertThat(logs).isEmpty();
    }

    @Test
    void testAuditLogWithOldAndNewValues() {
        // Given
        AuditLogs log = TestDataBuilder.createAuditLogWithValues(
                officer,
                EntityType.VERIFICATION_REQUEST,
                testEntityId,
                AuditAction.VERIFICATION_REQUEST_ASSIGNED,
                "PENDING",
                "IN_PROGRESS"
        );
        entityManager.persist(log);
        entityManager.flush();

        // When
        List<AuditLogs> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                EntityType.VERIFICATION_REQUEST, testEntityId);

        // Then
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getOldValue()).isEqualTo("PENDING");
        assertThat(logs.get(0).getNewValue()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void testMultipleEntityTypes() {
        // Given
        AuditLogs vrLog = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_CREATED);

        AuditLogs docLog = TestDataBuilder.createAuditLog(
                officer, EntityType.DOCUMENT, UUID.randomUUID(), AuditAction.DOCUMENT_UPLOADED);

        AuditLogs commentLog = TestDataBuilder.createAuditLog(
                officer, EntityType.COMMENT, UUID.randomUUID(), AuditAction.COMMENT_DELETED);

        entityManager.persist(vrLog);
        entityManager.persist(docLog);
        entityManager.persist(commentLog);
        entityManager.flush();

        // When
        List<AuditLogs> allLogs = auditLogRepository.findAllByOrderByTimestampDesc();

        // Then
        assertThat(allLogs).hasSize(3);
        assertThat(allLogs).extracting(AuditLogs::getEntityType)
                .containsExactlyInAnyOrder(
                        EntityType.VERIFICATION_REQUEST,
                        EntityType.DOCUMENT,
                        EntityType.COMMENT
                );
    }

    @Test
    void testMultipleActionsForSameEntity() {
        // Given
        AuditLogs createLog = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_CREATED);
        createLog.setTimestamp(LocalDateTime.now().minusDays(3));

        AuditLogs updateLog1 = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_ASSIGNED);
        updateLog1.setTimestamp(LocalDateTime.now().minusDays(2));

        AuditLogs updateLog2 = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_REASSIGNED);
        updateLog2.setTimestamp(LocalDateTime.now().minusDays(1));

        entityManager.persist(createLog);
        entityManager.persist(updateLog1);
        entityManager.persist(updateLog2);
        entityManager.flush();

        // When
        List<AuditLogs> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                EntityType.VERIFICATION_REQUEST, testEntityId);

        // Then
        assertThat(logs).hasSize(3);
        assertThat(logs.get(0).getAction()).isEqualTo(AuditAction.VERIFICATION_REQUEST_REASSIGNED);
        assertThat(logs.get(2).getAction()).isEqualTo(AuditAction.VERIFICATION_REQUEST_CREATED);
    }

    @Test
    void testAuditLogsByDifferentUsers() {
        // Given
        AuditLogs officerLog = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_REASSIGNED);
        officerLog.setTimestamp(LocalDateTime.now().minusDays(2));

        AuditLogs customerLog = TestDataBuilder.createAuditLog(
                customer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_CREATED);
        customerLog.setTimestamp(LocalDateTime.now().minusDays(1));

        entityManager.persist(officerLog);
        entityManager.persist(customerLog);
        entityManager.flush();

        // When
        List<AuditLogs> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                EntityType.VERIFICATION_REQUEST, testEntityId);

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getUser().getId()).isEqualTo(customer.getId());
        assertThat(logs.get(1).getUser().getId()).isEqualTo(officer.getId());
    }

    @Test
    void testTimestampPrecision() {
        // Given
        LocalDateTime specificTime = LocalDateTime.of(2024, 11, 15, 14, 30, 45);

        AuditLogs log = TestDataBuilder.createAuditLog(
                officer, EntityType.VERIFICATION_REQUEST, testEntityId, AuditAction.VERIFICATION_REQUEST_CREATED);
        log.setTimestamp(specificTime);
        entityManager.persist(log);
        entityManager.flush();

        // When
        List<AuditLogs> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                EntityType.VERIFICATION_REQUEST, testEntityId);

        // Then
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getTimestamp()).isEqualTo(specificTime);
    }
}