package in.zeta.service.impl;

import in.zeta.entity.Document;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.DocumentType;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.requests.DocumentUpdateRequest;
import in.zeta.dto.response.DocumentMetadataResponse;
import in.zeta.dto.response.DocumentResponse;
import in.zeta.repository.DocumentRepository;
import in.zeta.service.*;
import in.zeta.spectra.capture.SpectraLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserService userService;

    @Mock
    private VerificationRequestService verificationRequestService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OfficerAssignmentService officerAssignmentService;

    @Mock
    private AuditService auditService;

    @Mock
    private SpectraLogger logger;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private UUID testUserId;
    private UUID testDocumentId;
    private UUID testVerificationId;
    private Users testUser;
    private Document testDocument;
    private VerificationRequest testVerificationRequest;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testDocumentId = UUID.randomUUID();
        testVerificationId = UUID.randomUUID();

        testUser = Users.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .build();

        testVerificationRequest = VerificationRequest.builder()
                .id(testVerificationId)
                .customer(testUser)
                .requestor(testUser)
                .status(VerificationStatus.PENDING)
                .build();

        testDocument = Document.builder()
                .id(testDocumentId)
                .fileName("test.pdf")
                .contentType("application/pdf")
                .fileSize(BigDecimal.valueOf(1024))
                .documentType(DocumentType.VOTER_ID)
                .fileData("test data".getBytes())
                .verificationRequest(testVerificationRequest)
                .uploadedBy(testUser)
                .isActive(true)
                .build();

        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test data".getBytes()
        );

        ReflectionTestUtils.setField(documentService, "maxFileSize", 10485760L);
        ReflectionTestUtils.setField(documentService, "allowedContentTypes",
                Arrays.asList("application/pdf", "image/jpeg", "image/jpg", "image/png", "image/gif"));
    }

    @Test
    void uploadDocument_Success() {
        // Given
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(userService.getByIdOrThrow(testUserId, "User not found: " + testUserId))
                .thenReturn(testUser);
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        // When
        DocumentResponse result = documentService.uploadDocument(
                testVerificationId, testFile, DocumentType.VOTER_ID, "Test description", testUserId);

        // Then
        assertNotNull(result);
        assertEquals("test.pdf", result.getFileName());
        verify(documentRepository).save(any(Document.class));
        verify(verificationRequestService).save(any(VerificationRequest.class));
        verify(notificationService, times(1)).publishNotificationEvent(any());
    }

    @Test
    void uploadDocument_InvalidFileSize() {
        // Given
        ReflectionTestUtils.setField(documentService, "maxFileSize", 1L);

        // When & Then
        assertThrows(RuntimeException.class,
                () -> documentService.uploadDocument(testVerificationId, testFile,
                        DocumentType.ADDRESS_PROOF, "Test description", testUserId));
    }

    @Test
    void uploadDocument_InvalidContentType() {
        // Given
        MultipartFile invalidFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test data".getBytes());

        // When & Then
        assertThrows(RuntimeException.class,
                () -> documentService.uploadDocument(testVerificationId, invalidFile,
                        DocumentType.ADDRESS_PROOF, "Test description", testUserId));
    }

    @Test
    void getDocuments_Success() {
        // Given
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(userService.getByIdOrThrow(testUserId, "User not found: " + testUserId))
                .thenReturn(testUser);
        when(documentRepository.findByVerificationRequestAndUploadedBy(testVerificationRequest, testUser))
                .thenReturn(Arrays.asList(testDocument));

        // When
        List<DocumentResponse> result = documentService.getDocuments(testVerificationId, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getDocumentWithData_Success() {
        // Given
        when(documentRepository.findById(testDocumentId)).thenReturn(Optional.of(testDocument));

        // When
        DocumentResponse result = documentService.getDocumentWithData(testDocumentId, testVerificationId, testUserId);

        // Then
        assertNotNull(result);
        assertNotNull(result.getFileData());
    }

    @Test
    void getDocumentWithData_DocumentNotFound() {
        // Given
        when(documentRepository.findById(testDocumentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class,
                () -> documentService.getDocumentWithData(testDocumentId, testVerificationId, testUserId));
    }

    @Test
    void downloadMultipleDocuments_Success() {
        // Given
        List<UUID> documentIds = Arrays.asList(testDocumentId);
        when(documentRepository.findById(testDocumentId)).thenReturn(Optional.of(testDocument));

        // When
        byte[] result = documentService.downloadMultipleDocuments(documentIds, testVerificationId, testUserId);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void downloadMultipleDocuments_EmptyList() {
        // When & Then
        assertThrows(RuntimeException.class,
                () -> documentService.downloadMultipleDocuments(Arrays.asList(), testVerificationId, testUserId));
    }

    @Test
    void downloadAllDocuments_Success() {
        // Given
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(documentRepository.findByVerificationRequestAndIsActive(testVerificationRequest, true))
                .thenReturn(Arrays.asList(testDocument));

        // When
        byte[] result = documentService.downloadAllDocuments(testVerificationId, testUserId);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void downloadAllDocuments_NoDocuments() {
        // Given
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(documentRepository.findByVerificationRequestAndIsActive(testVerificationRequest, true))
                .thenReturn(Arrays.asList());

        // When & Then
        assertThrows(RuntimeException.class,
                () -> documentService.downloadAllDocuments(testVerificationId, testUserId));
    }

    @Test
    void updateDocument_Success() {
        // Given
        DocumentUpdateRequest updateRequest = DocumentUpdateRequest.builder()
                .file(testFile)
                .documentType(DocumentType.ADDRESS_PROOF)
                .description("Updated description")
                .build();

        when(documentRepository.findById(testDocumentId)).thenReturn(Optional.of(testDocument));
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        // When
        DocumentResponse result = documentService.updateDocument(
                testDocumentId, testVerificationId, updateRequest, testUserId);

        // Then
        assertNotNull(result);
        verify(documentRepository).save(any(Document.class));
        verify(notificationService, times(1)).publishNotificationEvent(any());
    }

    @Test
    void deleteDocument_Success() {
        // Given
        when(documentRepository.findById(testDocumentId)).thenReturn(Optional.of(testDocument));

        // When
        documentService.deleteDocument(testDocumentId, testVerificationId, testUserId);

        // Then
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void getDocumentsMetadata_Success() {
        // Given
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(userService.getByIdOrThrow(testUserId, "User not found: " + testUserId))
                .thenReturn(testUser);
        when(documentRepository.findByVerificationRequestAndUploadedBy(testVerificationRequest, testUser))
                .thenReturn(Arrays.asList(testDocument));

        // When
        List<DocumentMetadataResponse> result = documentService.getDocumentsMetadata(testVerificationId, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getDocumentMetadata_Success() {
        // Given
        when(documentRepository.findById(testDocumentId)).thenReturn(Optional.of(testDocument));

        // When
        DocumentMetadataResponse result = documentService.getDocumentMetadata(testDocumentId, testVerificationId);

        // Then
        assertNotNull(result);
    }

    @Test
    void getDocumentById_Success() {
        // Given
        when(documentRepository.findById(testDocumentId)).thenReturn(Optional.of(testDocument));

        // When
        DocumentResponse result = documentService.getDocumentById(testDocumentId);

        // Then
        assertNotNull(result);
    }
}