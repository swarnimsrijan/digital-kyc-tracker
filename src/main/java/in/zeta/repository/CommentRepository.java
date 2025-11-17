package in.zeta.repository;

import in.zeta.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByCreatedById(UUID userId);
    @Query("SELECT c FROM Comment c WHERE c.verificationRequest.assignedOfficer.id = :officerId")
    List<Comment> findByVerificationRequestAssignedOfficerId(@Param("officerId") UUID officerId);
    @Query("SELECT c FROM Comment c WHERE c.verificationRequest.customer.id = :customerId")
    List<Comment> findByVerificationRequestCustomerId(@Param("customerId") UUID customerId);
}