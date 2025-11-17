package in.zeta.dto.response;

import in.zeta.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private UUID id;
    private UUID verificationRequestId;
    private String fileName;
    private String contentType;
    private BigDecimal fileSize;
    private DocumentType documentType;
    private String description;
    private UUID uploadedBy;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private byte[] fileData;
}