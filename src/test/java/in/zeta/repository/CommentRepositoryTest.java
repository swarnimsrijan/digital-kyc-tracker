package in.zeta.repository;

import in.zeta.entity.Comment;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static in.zeta.enums.CommentType.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

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
    void testFindByCreatedById() {
        // Given
        Comment comment1 = TestDataBuilder.createComment(verificationRequest, officer);
        Comment comment2 = TestDataBuilder.createCommentWithText(verificationRequest, officer, "Second comment by officer");

        Comment commentByCustomer = TestDataBuilder.createComment(verificationRequest, customer);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(commentByCustomer);
        entityManager.flush();

        // When
        List<Comment> officerComments = commentRepository.findByCreatedById(officer.getId());

        // Then
        assertThat(officerComments).hasSize(2);
        assertThat(officerComments).allMatch(c -> c.getCreatedBy().getId().equals(officer.getId()));
    }

    @Test
    void testFindByVerificationRequestAssignedOfficerId() {
        // Given
        Comment comment1 = TestDataBuilder.createComment(verificationRequest, officer);
        Comment comment2 = TestDataBuilder.createComment(verificationRequest, customer);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.flush();

        // When
        List<Comment> comments = commentRepository.findByVerificationRequestAssignedOfficerId(officer.getId());

        // Then
        assertThat(comments).hasSize(2);
        assertThat(comments).allMatch(c ->
                c.getVerificationRequest().getAssignedOfficer().getId().equals(officer.getId()));
    }

    @Test
    void testFindByVerificationRequestCustomerId() {
        // Given
        Comment comment1 = TestDataBuilder.createComment(verificationRequest, customer);
        Comment comment2 = TestDataBuilder.createComment(verificationRequest, officer);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.flush();

        // When
        List<Comment> comments = commentRepository.findByVerificationRequestCustomerId(customer.getId());

        // Then
        assertThat(comments).hasSize(2);
        assertThat(comments).allMatch(c ->
                c.getVerificationRequest().getCustomer().getId().equals(customer.getId()));
    }

    @Test
    void testFindByVerificationRequestCustomerId_MultipleVerificationRequests() {
        // Given
        VerificationRequest vr2 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr2.setId(null);
        entityManager.persist(vr2);

        Comment comment1 = TestDataBuilder.createComment(verificationRequest, officer);
        Comment comment2 = TestDataBuilder.createComment(vr2, officer);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.flush();

        // When
        List<Comment> comments = commentRepository.findByVerificationRequestCustomerId(customer.getId());

        // Then
        assertThat(comments).hasSize(2);
        assertThat(comments).allMatch(c ->
                c.getVerificationRequest().getCustomer().getId().equals(customer.getId()));
    }

    @Test
    void testFindByCreatedById_NoComments() {
        // When
        List<Comment> comments = commentRepository.findByCreatedById(officer.getId());

        // Then
        assertThat(comments).isEmpty();
    }

    @Test
    void testFindByVerificationRequestAssignedOfficerId_UnassignedOfficer() {
        // Given
        Users anotherOfficer = TestDataBuilder.createOfficer();
        anotherOfficer.setId(null);
        anotherOfficer.setUsername("officer456");
        anotherOfficer.setEmail("officer456@test.com");
        entityManager.persist(anotherOfficer);
        entityManager.flush();

        Comment comment = TestDataBuilder.createComment(verificationRequest, officer);
        entityManager.persist(comment);
        entityManager.flush();

        // When
        List<Comment> comments = commentRepository.findByVerificationRequestAssignedOfficerId(anotherOfficer.getId());

        // Then
        assertThat(comments).isEmpty();
    }

    @Test
    void testCommentTypes() {
        // Given
        Comment generalComment = TestDataBuilder.createComment(verificationRequest, officer);
        generalComment.setCommentType(GENERAL);

        Comment internalComment = TestDataBuilder.createComment(verificationRequest, officer);
        internalComment.setCommentType(SYSTEM);
        internalComment.setCommentText("Internal review note");

        entityManager.persist(generalComment);
        entityManager.persist(internalComment);
        entityManager.flush();

        // When
        List<Comment> allComments = commentRepository.findByCreatedById(officer.getId());

        // Then
        assertThat(allComments).hasSize(2);
        assertThat(allComments).extracting(Comment::getCommentType)
                .containsExactlyInAnyOrder(GENERAL, SYSTEM);
    }
}