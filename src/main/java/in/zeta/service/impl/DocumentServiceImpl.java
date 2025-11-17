package in.zeta.service.impl;

import in.zeta.constants.Messages;
import in.zeta.entity.Document;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.*;
import static in.zeta.mapper.DocumentMapper.*;
import in.zeta.dto.requests.events.NotificationCreatedEvent;
import in.zeta.exception.DataNotFoundException;
import in.zeta.exception.InvalidOperationException;
import in.zeta.repository.DocumentRepository;
import in.zeta.dto.requests.DocumentUpdateRequest;
import in.zeta.dto.response.DocumentMetadataResponse;
import in.zeta.dto.response.DocumentResponse;
import in.zeta.service.*;
import in.zeta.spectra.capture.SpectraLogger;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static in.zeta.mapper.NotificationMapper.createNotificationEvent;

@Service
//@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    @Value("${file.upload.max-size}")
    private long maxFileSize;

    @Value("${file.upload.allowed-content-types}")
    private List<String> allowedContentTypes;

    private final SpectraLogger logger = OlympusSpectra.getLogger(DocumentServiceImpl.class);
    private final DocumentRepository documentRepository;
    private final UserService userService;
    private final VerificationRequestService verificationRequestService;
    private final NotificationService notificationService;
    private final OfficerAssignmentService officerAssignmentService;
    private final AuditService auditService;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               UserService userService,
                               VerificationRequestService verificationRequestService,
                               NotificationService notificationService,
                               OfficerAssignmentService officerAssignmentService,
                               AuditService auditService) {
        this.documentRepository = documentRepository;
        this.userService = userService;
        this.verificationRequestService = verificationRequestService;
        this.notificationService = notificationService;
        this.officerAssignmentService = officerAssignmentService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public DocumentResponse uploadDocument(UUID verificationId, MultipartFile file,
                                           DocumentType documentType, String description, UUID userId) {
        validateFile(file);

        VerificationRequest verificationRequest = getVerificationRequest(verificationId);
        Users user = getUser(userId);

        try {
            byte[] fileData = file.getBytes();
            String fileHash = calculateFileHash(fileData);

            Document document = Document.builder()
                    .verificationRequest(verificationRequest)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(BigDecimal.valueOf(file.getSize()))
                    .documentType(documentType)
                    .fileData(fileData)
                    .fileHash(fileHash)
                    .description(description)
                    .uploadedBy(user)
                    .isActive(true)
                    .build();

            verificationRequest.setStatus(VerificationStatus.DOCUMENT_UPLOADED);
            //here document has been uploaded so status is changed to DOCUMENT_UPLOADED
            //Create StatusUpdateEvent and publish it

            verificationRequestService.save(verificationRequest);

            Document savedDocument = documentRepository.save(document);

            logger.info("Creating notification for document upload")
                    .attr("for requestor", verificationRequest.getRequestor().getId())
                    .attr("documentId", savedDocument.getId())
                    .attr("verificationId", verificationId)
                    .attr("uploadedBy", userId)
                    .log();

            NotificationCreatedEvent requestorNotification = createNotificationEvent(
                    verificationRequest.getRequestor().getId(),
                    verificationRequest.getId(),
                    NotificationType.DOCUMENT_UPLOADED,
                    Messages.Notification.DOCUMENT_UPLOADED
            );
            notificationService.publishNotificationEvent(requestorNotification);

            logger.info("Notification created for requestor")
                    .attr("for requestor", verificationRequest.getRequestor().getId())
                    .attr("documentId", savedDocument.getId())
                    .attr("verificationId", verificationId)
                    .attr("uploadedBy", userId)
                    .log();

            if (verificationRequest.getAssignedOfficer() != null) {

                logger.info("Notification event created for assigned officer")
                        .attr("verificationRequestId", verificationRequest.getId())
//                        .attr("assignedOfficerId", selectedOfficer.getId())
                        .log();

                NotificationCreatedEvent notificationCreatedEvent = createNotificationEvent(
                        verificationRequest.getAssignedOfficer().getId(),
                        verificationRequest.getId(),
                        NotificationType.ASSIGNED_TO_OFFICER,
                        Messages.Notification.ASSIGNED_TO_OFFICER
                );
                notificationService.publishNotificationEvent(notificationCreatedEvent);
            } else {
                officerAssignmentService.assignOfficerToVerification(verificationId);
            }

            return convertToResponse(savedDocument, false);
        } catch (IOException e) {

            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<DocumentResponse> uploadMultipleDocuments(UUID verificationId, MultipartFile[] files,
                                                          DocumentType[] documentTypes, String[] descriptions, UUID userId) {


        if (files.length != documentTypes.length) {
            throw new RuntimeException("Number of files must match number of document types");
        }

        VerificationRequest verificationRequest = getVerificationRequest(verificationId);
        Users user = getUser(userId);
        List<DocumentResponse> responses = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            DocumentType docType = documentTypes[i];
            String description = (descriptions != null && descriptions.length > i) ? descriptions[i] : null;

            validateFile(file);

            try {
                byte[] fileData = file.getBytes();
                String fileHash = calculateFileHash(fileData);

                Document document = Document.builder()
                        .verificationRequest(verificationRequest)
                        .fileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .fileSize(BigDecimal.valueOf(file.getSize()))
                        .documentType(docType)
                        .fileData(fileData)
                        .fileHash(fileHash)
                        .description(description)
                        .uploadedBy(user)
                        .build();

                verificationRequest.setStatus(VerificationStatus.DOCUMENT_UPLOADED);
                verificationRequestService.save(verificationRequest);

                Document savedDocument = documentRepository.save(document);
                responses.add(convertToResponse(savedDocument, false));

            } catch (IOException e) {
                logger.error("Failed to upload document").attr("verificationId:", verificationId).attr("file name", file.getOriginalFilename()).log();
                throw new RuntimeException("Failed to upload document: " + file.getOriginalFilename(), e);
            }
        }

//        log.info("Successfully uploaded {} documents for verification: {}", responses.size(), verificationId);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(UUID verificationId, UUID customerId) {
        VerificationRequest verificationRequest = getVerificationRequest(verificationId);
        Users customer = getUser(customerId);
        List<Document> documents = documentRepository.findByVerificationRequestAndUploadedBy(verificationRequest, customer);

        return documents.stream()
                .map(doc -> convertToResponse(doc, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentWithData(UUID documentId, UUID verificationId, UUID userId) {
        Document document = getDocument(documentId);
        validateDocumentAccess(document, verificationId);

        return convertToResponse(document, true);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadMultipleDocuments(List<UUID> documentIds, UUID verificationId, UUID userId) {
        if (documentIds == null || documentIds.isEmpty()) {
            System.out.println("Document IDs list is empty");
            throw new RuntimeException("Document IDs cannot be empty");
        }

        List<Document> documents = documentIds.stream()
                .map(this::getDocument)
                .peek(doc -> validateDocumentAccess(doc, verificationId))
                .collect(Collectors.toList());

        return createZipFile(documents);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadAllDocuments(UUID verificationId, UUID userId)  {
        VerificationRequest verificationRequest = getVerificationRequest(verificationId);
        List<Document> documents = documentRepository.findByVerificationRequestAndIsActive(verificationRequest, true);

        if (documents.isEmpty()) {
            throw new RuntimeException("No documents found for verification request: ");
        }

        return createZipFile(documents);
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(UUID documentId, UUID verificationId,
                                           DocumentUpdateRequest updateRequest, UUID userId) {
        Document document = getDocument(documentId);
        validateDocumentAccess(document, verificationId);
        validateDocumentOwnership(document, userId);

        VerificationRequest verificationRequest = getVerificationRequest(verificationId);

        try {
            if (updateRequest.getFile() != null && !updateRequest.getFile().isEmpty()) {
                MultipartFile file = updateRequest.getFile();
                validateFile(file);

                byte[] fileData = file.getBytes();
                document.setFileData(fileData);
                document.setFileName(file.getOriginalFilename());
                document.setContentType(file.getContentType());
                document.setFileSize(BigDecimal.valueOf(file.getSize()));
                document.setFileHash(calculateFileHash(fileData));
            }

            if (updateRequest.getDocumentType() != null) {
                document.setDocumentType(updateRequest.getDocumentType());
            }

            if (updateRequest.getDescription() != null) {
                document.setDescription(updateRequest.getDescription());
            }

            Document updatedDocument = documentRepository.save(document);


            NotificationCreatedEvent requestorNotification = createNotificationEvent(
                    verificationRequest.getRequestor().getId(),
                    verificationRequest.getId(),
                    NotificationType.DOCUMENT_UPDATED,
                    Messages.Notification.DOCUMENT_UPDATED
            );
            notificationService.publishNotificationEvent(requestorNotification);


            // Notify officer (if assigned)
            if (verificationRequest.getAssignedOfficer() != null) {

                NotificationCreatedEvent officerNotification = createNotificationEvent(
                        verificationRequest.getAssignedOfficer().getId(),
                        verificationRequest.getId(),
                        NotificationType.DOCUMENT_UPDATED,
                        Messages.Notification.DOCUMENT_UPDATED
                );
                notificationService.publishNotificationEvent(officerNotification);
            }

            return convertToResponse(updatedDocument, false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update document: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<DocumentResponse> updateMultipleDocuments(UUID verificationId, List<UUID> documentIds,
                                                          MultipartFile[] files, DocumentType[] documentTypes,
                                                          String[] descriptions, UUID userId) {
        List<DocumentResponse> responses = new ArrayList<>();

        for (int i = 0; i < documentIds.size(); i++) {
            UUID documentId = documentIds.get(i);
            Document document = getDocument(documentId);
            validateDocumentAccess(document, verificationId);
            validateDocumentOwnership(document, userId);

            try {
                if (files != null && files.length > i && files[i] != null) {
                    MultipartFile file = files[i];
                    validateFile(file);

                    byte[] fileData = file.getBytes();
                    document.setFileData(fileData);
                    document.setFileName(file.getOriginalFilename());
                    document.setContentType(file.getContentType());
                    document.setFileSize(BigDecimal.valueOf(file.getSize()));
                    document.setFileHash(calculateFileHash(fileData));
                }

                if (documentTypes != null && documentTypes.length > i && documentTypes[i] != null) {
                    document.setDocumentType(documentTypes[i]);
                }

                if (descriptions != null && descriptions.length > i && descriptions[i] != null) {
                    document.setDescription(descriptions[i]);
                }

                Document updatedDocument = documentRepository.save(document);
                responses.add(convertToResponse(updatedDocument, false));

            } catch (IOException e) {
//                log.error("Failed to update document: {}", documentId, e);
                throw new RuntimeException("Failed to update document: " + documentId, e);
            }
        }

//        log.info("Successfully updated {} documents for verification: {}", responses.size(), verificationId);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentMetadataResponse> getDocumentsMetadata(UUID verificationId, UUID customerId) {
        VerificationRequest verificationRequest = getVerificationRequest(verificationId);
        Users Customer = getUser(customerId);
        List<Document> documents = documentRepository.findByVerificationRequestAndUploadedBy(verificationRequest, Customer);

        return documents.stream()
                .map(document ->convertToMetadataResponse(document))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentMetadataResponse getDocumentMetadata(UUID documentId, UUID verificationId) {
        Document document = getDocument(documentId);
        validateDocumentAccess(document, verificationId);
        return convertToMetadataResponse(document);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID documentId, UUID verificationId, UUID userId) {
        Document document = getDocument(documentId);
        validateDocumentAccess(document, verificationId);
        validateDocumentOwnership(document, userId);

        document.setIsActive(false);
        documentRepository.save(document);

//        log.info("Document soft deleted: {}", documentId);
    }

    @Override
    @Transactional
    public void deleteMultipleDocuments(UUID verificationId, UUID userId, List<UUID> documentIds) {
//        VerificationRequest verificationRequest = getVerificationRequest(verificationId);

        List<Document> documents = documentRepository.findByVerificationRequestIdAndUploadedById(verificationId, userId, documentIds);
        documents.forEach(doc -> {
            validateDocumentOwnership(doc, userId);
            doc.setIsActive(false);
        });

        documentRepository.saveAll(documents);
//        log.info("All documents soft deleted for verification: {}", verificationId);
    }

    @Override
    public List<Document> findByVerificationRequestIdAndUserID(VerificationRequest verificationRequest, Users customer){
        return documentRepository.findByVerificationRequestAndUploadedBy(verificationRequest, customer);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum limit of 10MB");
        }

        if (!allowedContentTypes.contains(file.getContentType())) {
            throw new RuntimeException("File type not allowed. Only PDF, JPEG, JPG, PNG, and GIF are supported");
        }
    }

    private VerificationRequest getVerificationRequest(UUID verificationId) {
        return verificationRequestService.getByIdOrThrow(verificationId);
    }

    private Users getUser(UUID userId) {
        return userService.getByIdOrThrow(userId, "User not found: " + userId);
    }

    private Document getDocument(UUID documentId) {
        try {
            logger.info("Fetching document by ID")
                    .attr("documentId", documentId.toString())
                    .log();

            return documentRepository.findById(documentId)
                    .orElseThrow(() -> new DataNotFoundException("document not found", "id", documentId));
        } catch (DataNotFoundException e) {
            logger.error("Document not found")
                    .attr("tableName", e.getTableName())
                    .attr("fieldName", e.getFieldName())
                    .attr("fieldValue", e.getFieldValue().toString())
                    .log();
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching document")
                    .attr("documentId", documentId.toString())
                    .attr("error", e.getMessage())
                    .log();
            throw new RuntimeException("Failed to fetch document", e);
        }
    }

    private void validateDocumentAccess(Document document, UUID verificationId) {
        try {
            if (!document.getVerificationRequest().getId().equals(verificationId)) {

                logger.error("Document access validation failed")
                        .attr("documentId", document.getId().toString())
                        .attr("verificationId", verificationId.toString())
                        .attr("documentVerificationId", document.getVerificationRequest().getId().toString())
                        .log();

                throw new DataNotFoundException("document", "verification_request_id", verificationId,
                        "Document does not belong to this verification request");
            }
            logger.info("Document access validated successfully")
                    .attr("documentId", document.getId().toString())
                    .attr("verificationId", verificationId.toString())
                    .log();

        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error validating document access")
                    .attr("documentId", document.getId().toString())
                    .attr("verificationId", verificationId.toString())
                    .attr("error", e.getMessage())
                    .log();
            throw new RuntimeException("Failed to validate document access", e);
        }
    }

    private void validateDocumentOwnership(Document document, UUID userId) {
        try {
            if (!document.getUploadedBy().getId().equals(userId)) {
                logger.error("Document ownership validation failed")
                        .attr("documentId", document.getId().toString())
                        .attr("userId", userId.toString())
                        .attr("documentOwnerId", document.getUploadedBy().getId().toString())
                        .log();
                throw new InvalidOperationException("You don't have permission to modify this document");
            }

            logger.info("Document ownership validated successfully")
                    .attr("documentId", document.getId().toString())
                    .attr("userId", userId.toString())
                    .log();
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error validating document ownership")
                    .attr("documentId", document.getId().toString())
                    .attr("userId", userId.toString())
                    .attr("error", e.getMessage())
                    .log();
            throw new RuntimeException("Failed to validate document ownership", e);
        }
    }

    private String calculateFileHash(byte[] fileData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileData);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to calculate file hash")
                    .attr("error", e.getMessage())
                    .log();
            return null;
        }
    }

    private byte[] createZipFile(List<Document> documents) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Document document : documents) {
                ZipEntry entry = new ZipEntry(document.getFileName());
                zos.putNextEntry(entry);
                zos.write(document.getFileData());
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
//            log.error("Error creating ZIP file", e);
            throw new RuntimeException("Failed to create ZIP file", e);
        }
    }

    @Override
    public List<Document> findByVerificationRequestId(UUID verificationId){
        return documentRepository.findByVerificationRequestId(verificationId);
    }

    @Override
    public DocumentResponse getDocumentById(UUID documentId){
        Document document = getDocument(documentId);
        return convertToResponse(document, false);
    }
}