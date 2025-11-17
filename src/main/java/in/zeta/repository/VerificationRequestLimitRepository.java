package in.zeta.repository;

import in.zeta.entity.VerificationRequestLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationRequestLimitRepository extends JpaRepository<VerificationRequestLimit, UUID> {
    Optional<VerificationRequestLimit> findByCustomerIdAndVerificationRequestorIdAndYear(
            UUID customerId, UUID requestorId, Integer year);
    @Query("SELECT SUM(vrl.requestCount) FROM VerificationRequestLimit vrl " +
            "WHERE vrl.customer.id = :customerId AND vrl.year = :year")
    Integer getTotalRequestsForCustomerInYear(@Param("customerId") UUID customerId, @Param("year") Integer year);
    List<VerificationRequestLimit> findByCustomerIdAndYear(UUID customerId, Integer year);
}