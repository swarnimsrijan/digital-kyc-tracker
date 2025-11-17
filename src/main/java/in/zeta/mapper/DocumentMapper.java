package in.zeta.mapper;

import in.zeta.entity.Document;
import in.zeta.dto.response.DocumentMetadataResponse;
import in.zeta.dto.response.DocumentResponse;

public class DocumentMapper {
    public static DocumentResponse convertToResponse(Document document, boolean includeFileData) {
        DocumentResponse.DocumentResponseBuilder builder = DocumentResponse.builder()
                .id(document.getId())
                .verificationRequestId(document.getVerificationRequest().getId())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .documentType(document.getDocumentType())
                .description(document.getDescription())
                .uploadedBy(document.getUploadedBy().getId())
                .uploadedAt(document.getUploadedAt())
                .updatedAt(document.getUpdatedAt());
        if(includeFileData) {
            builder.fileData(document.getFileData());
        }
        return builder.build();
    }
    public static DocumentMetadataResponse convertToMetadataResponse(Document document) {
        DocumentMetadataResponse.DocumentMetadataResponseBuilder builder = DocumentMetadataResponse.builder()
                .id(document.getId())
                .verificationRequestId(document.getVerificationRequest().getId())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .documentType(document.getDocumentType())
                .description(document.getDescription())
                .fileHash(document.getFileHash())
                .uploadedBy(document.getUploadedBy().getId())
                .uploadedAt(document.getUploadedAt())
                .updatedAt(document.getUpdatedAt())
                .isActive(document.getIsActive())
                .status(document.getVerificationRequest().getStatus());

        if(document.getVerificationRequest().getApprovedAt() != null) {
            builder.approvedAt(document.getVerificationRequest().getApprovedAt());
        }

//        if(document.getVerificationRequest().getDeletedAt() != null) {
//            builder.deletedAt(document.getVerificationRequest().getDeletedAt());
//        }

        return builder.build();
    }

    private DocumentMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

}
