package in.zeta.dto.requests;

import in.zeta.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUpdateRequest {
    private MultipartFile file;
    private DocumentType documentType;
    private String description;
}