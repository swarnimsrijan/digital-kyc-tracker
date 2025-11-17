package in.zeta.repository;


import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.VerificationStatus;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {
    List<VerificationRequest> findByCustomer(Users customer);
    List<VerificationRequest> findByRequestor(Users requestor);
    List<VerificationRequest> findByAssignedOfficer(Users officer);
    List<VerificationRequest> findByStatus(VerificationStatus status);

    @Query("SELECT vr FROM VerificationRequest vr WHERE vr.status IN :statuses")
    List<VerificationRequest> findByStatusIn(@Param("statuses") List<VerificationStatus> statuses);

    @Query("SELECT vr FROM VerificationRequest vr WHERE vr.assignedOfficer IS NULL AND vr.status = :status")
    List<VerificationRequest> findUnassignedByStatus(@Param("status") VerificationStatus status);

    Optional<VerificationRequest> findById(UUID id);

    List<VerificationRequest> findByCustomerId(UUID customerId);

    List<VerificationRequest> findByRequestorId(UUID requestorId);

    List<VerificationRequest> findByAssignedOfficerId(UUID officerId);

    List<VerificationRequest> findByAssignedOfficerAndStatus(Users assignedOfficer, VerificationStatus status);
}
