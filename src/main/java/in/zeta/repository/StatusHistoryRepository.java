package in.zeta.repository;

import in.zeta.entity.StatusHistory;
import in.zeta.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {
    @Query("SELECT sh FROM StatusHistory sh WHERE sh.verificationRequest = :verificationRequest ORDER BY sh.changedAt DESC")
    List<StatusHistory> findByVerificationRequestOrderByChangedAtDesc(@Param("verificationRequest") VerificationRequest verificationRequest);
    @Query("SELECT sh FROM StatusHistory sh WHERE sh.verificationRequest = :verificationRequest ORDER BY sh.changedAt DESC")
    Optional<StatusHistory> findLatestStatusHistory(@Param("verificationRequest") VerificationRequest verificationRequest);
}
