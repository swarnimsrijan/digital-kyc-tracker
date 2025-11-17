package in.zeta.repository;

import in.zeta.entity.Document;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByVerificationRequestAndIsActive(VerificationRequest verificationRequest, Boolean isActive);

    List<Document> findByVerificationRequestAndUploadedBy(VerificationRequest verificationRequest, Users uploadedBy);

    @Query("SELECT d FROM Document d WHERE d.verificationRequest.id = :verificationRequestId AND d.isActive = true")
    List<Document> findByVerificationRequestId(@Param("verificationRequestId") UUID verificationRequestId);

    Optional<Document> findByFileHashAndVerificationRequest(String fileHash, VerificationRequest verificationRequest);

    @Query("SELECT d FROM Document d WHERE d.verificationRequest.id = :verificationRequestId AND d.uploadedBy.id = :uploadedById AND d.id IN :documentIds AND d.isActive = true")
    List<Document> findByVerificationRequestIdAndUploadedById(
            @Param("verificationRequestId") UUID verificationRequestId,
            @Param("uploadedById") UUID uploadedById,
            @Param("documentIds") List<UUID> documentIds
    );

    @Query("SELECT COUNT(d) FROM Document d WHERE d.verificationRequest.id = :verificationRequestId AND d.isActive = true")
    long countActiveDocumentsByVerificationRequestId(@Param("verificationRequestId") UUID verificationRequestId);

    @Query("SELECT d FROM Document d WHERE d.uploadedBy.id = :uploadedById AND d.isActive = true")
    List<Document> findByUploadedById(@Param("uploadedById") UUID uploadedById);
}