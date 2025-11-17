package in.zeta.mapper;

import in.zeta.entity.Document;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.DocumentType;
import in.zeta.enums.VerificationStatus;
import in.zeta.dto.response.DocumentMetadataResponse;
import in.zeta.dto.response.DocumentResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DocumentMapperTest {

    @Test
    void convertToResponse_withFileData_success() {
        Document document = createTestDocument();

        DocumentResponse response = DocumentMapper.convertToResponse(document, true);

        assertNotNull(response);
        assertEquals(document.getId(), response.getId());
        assertEquals(document.getFileName(), response.getFileName());
        assertNotNull(response.getFileData());
    }

    @Test
    void convertToResponse_withoutFileData_success() {
        Document document = createTestDocument();

        DocumentResponse response = DocumentMapper.convertToResponse(document, false);

        assertNotNull(response);
        assertEquals(document.getId(), response.getId());
        assertNull(response.getFileData());
    }

    @Test
    void convertToMetadataResponse_success() {
        Document document = createTestDocument();

        DocumentMetadataResponse response = DocumentMapper.convertToMetadataResponse(document);

        assertNotNull(response);
        assertEquals(document.getId(), response.getId());
        assertEquals(document.getFileHash(), response.getFileHash());
        assertEquals(document.getIsActive(), response.getIsActive());
    }

    private Document createTestDocument() {
        Users user = Users.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .build();

        VerificationRequest verificationRequest = VerificationRequest.builder()
                .id(UUID.randomUUID())
                .status(VerificationStatus.PENDING)
                .approvedAt(LocalDateTime.now())
                .build();

        return Document.builder()
                .id(UUID.randomUUID())
                .verificationRequest(verificationRequest)
                .fileName("test.pdf")
                .contentType("application/pdf")
                .fileSize(BigDecimal.valueOf(1024))
                .documentType(DocumentType.AADHAAR_FRONT)
                .fileData("test data".getBytes())
                .fileHash("testhash")
                .description("Test document")
                .uploadedBy(user)
                .uploadedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }
}