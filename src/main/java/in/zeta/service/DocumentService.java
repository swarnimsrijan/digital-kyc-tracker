package in.zeta.service;

import in.zeta.entity.Document;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.DocumentType;
import in.zeta.dto.requests.DocumentUpdateRequest;
import in.zeta.dto.response.DocumentMetadataResponse;
import in.zeta.dto.response.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface DocumentService {



    DocumentResponse uploadDocument(UUID verificationId, MultipartFile file,
                                    DocumentType documentType, String description, UUID userId);

    List<DocumentResponse> uploadMultipleDocuments(UUID verificationId, MultipartFile[] files,
                                                   DocumentType[] documentTypes, String[] descriptions, UUID userId);

    List<DocumentResponse> getDocuments(UUID verificationId, UUID customerId);

    DocumentResponse getDocumentWithData(UUID documentId, UUID verificationId, UUID userId);

    byte[] downloadMultipleDocuments(List<UUID> documentIds, UUID verificationId, UUID userId);

    byte[] downloadAllDocuments(UUID verificationId, UUID userId);

    DocumentResponse updateDocument(UUID documentId, UUID verificationId,
                                    DocumentUpdateRequest updateRequest, UUID userId);

    List<DocumentResponse> updateMultipleDocuments(UUID verificationId, List<UUID> documentIds,
                                                   MultipartFile[] files, DocumentType[] documentTypes,
                                                   String[] descriptions, UUID userId);
    List<DocumentMetadataResponse> getDocumentsMetadata(UUID verificationId, UUID customerId);


    DocumentMetadataResponse getDocumentMetadata(UUID documentId, UUID verificationId);

    void deleteDocument(UUID documentId, UUID verificationId, UUID userId);

    void deleteMultipleDocuments(UUID verificationId, UUID userId, List<UUID> documentIds);

    List<Document> findByVerificationRequestIdAndUserID(VerificationRequest verificationRequest, Users customer);

    List<Document> findByVerificationRequestId(UUID verificationId);

    DocumentResponse getDocumentById(UUID documentId);


}