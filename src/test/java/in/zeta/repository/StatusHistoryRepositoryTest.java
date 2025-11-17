package in.zeta.repository;

import in.zeta.entity.StatusHistory;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StatusHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    private Users customer;
    private Users requestor;
    private Users officer;
    private VerificationRequest verificationRequest;

    @BeforeEach
    void setUp() {
        customer = TestDataBuilder.createCustomer();
        requestor = TestDataBuilder.createRequestor();
        officer = TestDataBuilder.createOfficer();

        entityManager.persist(customer);
        entityManager.persist(requestor);
        entityManager.persist(officer);

        verificationRequest = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        entityManager.persist(verificationRequest);
        entityManager.flush();
    }

    @Test
    void testFindByVerificationRequestOrderByChangedAtDesc() {
        // Given
        StatusHistory history1 = TestDataBuilder.createStatusHistory(verificationRequest, officer);
        history1.setFromStatus(VerificationStatus.PENDING);
        history1.setToStatus(VerificationStatus.IN_REVIEW);
        history1.setChangedAt(LocalDateTime.now().minusDays(2));

        StatusHistory history2 = TestDataBuilder.createStatusHistory(verificationRequest, officer);
        history2.setFromStatus(VerificationStatus.IN_REVIEW);
        history2.setToStatus(VerificationStatus.APPROVED);
        history2.setChangedAt(LocalDateTime.now().minusDays(1));

        StatusHistory history3 = TestDataBuilder.createStatusHistory(verificationRequest, officer);
        history3.setFromStatus(VerificationStatus.APPROVED);
        history3.setToStatus(VerificationStatus.APPROVED);
        history3.setChangedAt(LocalDateTime.now());

        entityManager.persist(history1);
        entityManager.persist(history2);
        entityManager.persist(history3);
        entityManager.flush();

        // When
        List<StatusHistory> histories =
                statusHistoryRepository.findByVerificationRequestOrderByChangedAtDesc(verificationRequest);

        // Then
        assertThat(histories).hasSize(3);
        // Verify order - most recent first
        assertThat(histories.get(0).getToStatus()).isEqualTo(VerificationStatus.APPROVED);
        assertThat(histories.get(1).getToStatus()).isEqualTo(VerificationStatus.APPROVED);
        assertThat(histories.get(2).getToStatus()).isEqualTo(VerificationStatus.IN_REVIEW);
    }

    @Test
    void testFindByVerificationRequestOrderByChangedAtDesc_NoHistory() {
        // When
        List<StatusHistory> histories =
                statusHistoryRepository.findByVerificationRequestOrderByChangedAtDesc(verificationRequest);

        // Then
        assertThat(histories).isEmpty();
    }

    @Test
    void testFindLatestStatusHistory_NoHistory() {
        // When
        Optional<StatusHistory> latestHistory =
                statusHistoryRepository.findLatestStatusHistory(verificationRequest);

        // Then
        assertThat(latestHistory).isEmpty();
    }

    @Test
    void testStatusHistoryWithReasons() {
        // Given
        StatusHistory history1 = TestDataBuilder.createStatusHistory(verificationRequest, officer);
        history1.setReason("Starting verification process");
        history1.setToStatus(VerificationStatus.IN_REVIEW);

        StatusHistory history2 = TestDataBuilder.createStatusHistory(verificationRequest, officer);
        history2.setReason("All documents verified successfully");
        history2.setToStatus(VerificationStatus.APPROVED);
        history2.setChangedAt(LocalDateTime.now());

        entityManager.persist(history1);
        entityManager.persist(history2);
        entityManager.flush();

        // When
        List<StatusHistory> histories =
                statusHistoryRepository.findByVerificationRequestOrderByChangedAtDesc(verificationRequest);

        // Then
        assertThat(histories).hasSize(2);
        assertThat(histories).allMatch(h -> h.getReason() != null && !h.getReason().isEmpty());
    }

    @Test
    void testStatusTransitionFlow() {
        // Given - Complete workflow from PENDING to APPROVED
        StatusHistory[] transitions = {
                createTransition(VerificationStatus.PENDING, VerificationStatus.DOCUMENT_UPLOADED, 5),
                createTransition(VerificationStatus.DOCUMENT_UPLOADED, VerificationStatus.IN_REVIEW, 4),
                createTransition(VerificationStatus.IN_REVIEW, VerificationStatus.DOCUMENT_UPDATED, 3),
                createTransition(VerificationStatus.DOCUMENT_UPDATED, VerificationStatus.APPROVED, 2),
                createTransition(VerificationStatus.APPROVED, VerificationStatus.APPROVED, 1)
        };

        for (StatusHistory transition : transitions) {
            entityManager.persist(transition);
        }
        entityManager.flush();

        // When
        List<StatusHistory> histories =
                statusHistoryRepository.findByVerificationRequestOrderByChangedAtDesc(verificationRequest);

        // Then
        assertThat(histories).hasSize(5);
        // Verify complete flow in reverse chronological order
        assertThat(histories.get(0).getToStatus()).isEqualTo(VerificationStatus.APPROVED);
        assertThat(histories.get(4).getFromStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    private StatusHistory createTransition(VerificationStatus from, VerificationStatus to, int daysAgo) {
        StatusHistory history = TestDataBuilder.createStatusHistory(verificationRequest, officer);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedAt(LocalDateTime.now().minusDays(daysAgo));
        history.setReason("Status transition from " + from + " to " + to);
        return history;
    }
}