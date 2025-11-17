package in.zeta.repository;

import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VerificationRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    private Users customer;
    private Users requestor;
    private Users officer;
    private Users officer2;

    @BeforeEach
    void setUp() {
        customer = TestDataBuilder.createCustomer();
        requestor = TestDataBuilder.createRequestor();
        officer = TestDataBuilder.createOfficer();

        officer2 = TestDataBuilder.createOfficerWithUsername("officer456", "officer2@test.com");

        entityManager.persist(customer);
        entityManager.persist(requestor);
        entityManager.persist(officer);
        entityManager.persist(officer2);
        entityManager.flush();
    }

    @Test
    void testFindByCustomer() {
        // Given
        VerificationRequest vr1 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr1.setId(null);
        VerificationRequest vr2 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr2.setId(null);

        entityManager.persist(vr1);
        entityManager.persist(vr2);
        entityManager.flush();

        // When
        List<VerificationRequest> requests = verificationRequestRepository.findByCustomer(customer);

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests).allMatch(vr -> vr.getCustomer().getId().equals(customer.getId()));
    }

    @Test
    void testFindByRequestor() {
        // Given
        VerificationRequest vr1 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr1.setId(null);
        VerificationRequest vr2 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr2.setId(null);

        entityManager.persist(vr1);
        entityManager.persist(vr2);
        entityManager.flush();

        // When
        List<VerificationRequest> requests = verificationRequestRepository.findByRequestor(requestor);

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests).allMatch(vr -> vr.getRequestor().getId().equals(requestor.getId()));
    }

    @Test
    void testFindByAssignedOfficer() {
        // Given
        VerificationRequest vr1 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr1.setId(null);
        VerificationRequest vr2 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr2.setId(null);

        VerificationRequest vr3 = TestDataBuilder.createVerificationRequest(customer, requestor, officer2);
        vr3.setId(null);

        entityManager.persist(vr1);
        entityManager.persist(vr2);
        entityManager.persist(vr3);
        entityManager.flush();

        // When
        List<VerificationRequest> officer1Requests = verificationRequestRepository.findByAssignedOfficer(officer);

        // Then
        assertThat(officer1Requests).hasSize(2);
        assertThat(officer1Requests).allMatch(vr -> vr.getAssignedOfficer().getId().equals(officer.getId()));
    }

    @Test
    void testFindByStatus() {
        // Given
        VerificationRequest pending = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.PENDING);
        pending.setId(null);

        VerificationRequest inProgress = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.IN_REVIEW);
        inProgress.setId(null);

        VerificationRequest completed = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.APPROVED);
        completed.setId(null);

        entityManager.persist(pending);
        entityManager.persist(inProgress);
        entityManager.persist(completed);
        entityManager.flush();

        // When
        List<VerificationRequest> pendingRequests =
                verificationRequestRepository.findByStatus(VerificationStatus.PENDING);

        // Then
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0).getStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void testFindByStatusIn() {
        // Given
        VerificationRequest pending = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.PENDING);
        pending.setId(null);

        VerificationRequest inProgress = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.IN_REVIEW);
        inProgress.setId(null);

        VerificationRequest completed = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.APPROVED);
        completed.setId(null);

        entityManager.persist(pending);
        entityManager.persist(inProgress);
        entityManager.persist(completed);
        entityManager.flush();

        // When
        List<VerificationStatus> statuses = Arrays.asList(
                VerificationStatus.PENDING,
                VerificationStatus.IN_REVIEW
        );
        List<VerificationRequest> requests = verificationRequestRepository.findByStatusIn(statuses);

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(VerificationRequest::getStatus)
                .containsExactlyInAnyOrder(VerificationStatus.PENDING, VerificationStatus.IN_REVIEW);
    }

    @Test
    void testFindUnassignedByStatus() {
        // Given
        VerificationRequest assigned = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.PENDING);
        assigned.setId(null);

        VerificationRequest unassigned = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, null, VerificationStatus.PENDING);
        unassigned.setId(null);
        unassigned.setAssignedOfficer(null);

        entityManager.persist(assigned);
        entityManager.persist(unassigned);
        entityManager.flush();

        // When
        List<VerificationRequest> unassignedRequests =
                verificationRequestRepository.findUnassignedByStatus(VerificationStatus.PENDING);

        // Then
        assertThat(unassignedRequests).hasSize(1);
        assertThat(unassignedRequests.get(0).getAssignedOfficer()).isNull();
        assertThat(unassignedRequests.get(0).getStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void testFindById() {
        // Given
        VerificationRequest vr = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr.setId(null);
        entityManager.persist(vr);
        entityManager.flush();

        // When
        Optional<VerificationRequest> foundRequest = verificationRequestRepository.findById(vr.getId());

        // Then
        assertThat(foundRequest).isPresent();
        assertThat(foundRequest.get().getRequestReason()).isEqualTo(vr.getRequestReason());
    }

    @Test
    void testFindByCustomerId() {
        // Given
        VerificationRequest vr1 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr1.setId(null);
        VerificationRequest vr2 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr2.setId(null);

        entityManager.persist(vr1);
        entityManager.persist(vr2);
        entityManager.flush();

        // When
        List<VerificationRequest> requests = verificationRequestRepository.findByCustomerId(customer.getId());

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests).allMatch(vr -> vr.getCustomer().getId().equals(customer.getId()));
    }

    @Test
    void testFindByRequestorId() {
        // Given
        VerificationRequest vr1 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr1.setId(null);
        VerificationRequest vr2 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr2.setId(null);

        entityManager.persist(vr1);
        entityManager.persist(vr2);
        entityManager.flush();

        // When
        List<VerificationRequest> requests = verificationRequestRepository.findByRequestorId(requestor.getId());

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests).allMatch(vr -> vr.getRequestor().getId().equals(requestor.getId()));
    }

    @Test
    void testFindByAssignedOfficerId() {
        // Given
        VerificationRequest vr1 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr1.setId(null);
        VerificationRequest vr2 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr2.setId(null);

        entityManager.persist(vr1);
        entityManager.persist(vr2);
        entityManager.flush();

        // When
        List<VerificationRequest> requests = verificationRequestRepository.findByAssignedOfficerId(officer.getId());

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests).allMatch(vr -> vr.getAssignedOfficer().getId().equals(officer.getId()));
    }

    @Test
    void testFindByAssignedOfficerAndStatus() {
        // Given
        VerificationRequest pending = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.PENDING);
        pending.setId(null);

        VerificationRequest inProgress = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.IN_REVIEW);
        inProgress.setId(null);

        VerificationRequest completed = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.APPROVED);
        completed.setId(null);

        entityManager.persist(pending);
        entityManager.persist(inProgress);
        entityManager.persist(completed);
        entityManager.flush();

        // When
        List<VerificationRequest> pendingRequests =
                verificationRequestRepository.findByAssignedOfficerAndStatus(officer, VerificationStatus.PENDING);

        // Then
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0).getStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(pendingRequests.get(0).getAssignedOfficer().getId()).isEqualTo(officer.getId());
    }

    @Test
    void testFindByStatus_MultipleStatuses() {
        // Given
        VerificationRequest approved = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.APPROVED);
        approved.setId(null);

        VerificationRequest rejected = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.REJECTED);
        rejected.setId(null);

        entityManager.persist(approved);
        entityManager.persist(rejected);
        entityManager.flush();

        // When
        List<VerificationRequest> approvedRequests =
                verificationRequestRepository.findByStatus(VerificationStatus.APPROVED);
        List<VerificationRequest> rejectedRequests =
                verificationRequestRepository.findByStatus(VerificationStatus.REJECTED);

        // Then
        assertThat(approvedRequests).hasSize(1);
        assertThat(rejectedRequests).hasSize(1);
    }

    @Test
    void testFindUnassignedByStatus_EmptyResult() {
        // Given
        VerificationRequest assigned = TestDataBuilder.createVerificationRequestWithStatus(
                customer, requestor, officer, VerificationStatus.PENDING);
        assigned.setId(null);
        entityManager.persist(assigned);
        entityManager.flush();

        // When
        List<VerificationRequest> unassignedRequests =
                verificationRequestRepository.findUnassignedByStatus(VerificationStatus.PENDING);

        // Then
        assertThat(unassignedRequests).isEmpty();
    }

    @Test
    void testVerificationRequestWithTimestamps() {
        // Given
        VerificationRequest vr = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr.setId(null);
        vr.setStatus(VerificationStatus.APPROVED);
        vr.setApprovedAt(LocalDateTime.now());
        entityManager.persist(vr);
        entityManager.flush();

        // When
        Optional<VerificationRequest> foundRequest = verificationRequestRepository.findById(vr.getId());

        // Then
        assertThat(foundRequest).isPresent();
        assertThat(foundRequest.get().getCreatedAt()).isNotNull();
        assertThat(foundRequest.get().getApprovedAt()).isNotNull();
    }
}