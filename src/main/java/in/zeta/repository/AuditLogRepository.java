package in.zeta.repository;


import in.zeta.entity.AuditLogs;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogs, UUID> {

    @Query("SELECT a FROM AuditLogs a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.timestamp DESC")
    List<AuditLogs> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            @Param("entityType") EntityType entityType,
            @Param("entityId") UUID entityId
    );

    List<AuditLogs> findByActionOrderByTimestampDesc(AuditAction action);

    List<AuditLogs> findByUserIdOrderByTimestampDesc(UUID userId);

    List<AuditLogs> findAllByOrderByTimestampDesc();
}