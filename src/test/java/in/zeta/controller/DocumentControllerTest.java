package in.zeta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.enums.DocumentType;
import in.zeta.dto.response.DocumentResponse;
import in.zeta.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID VERIFICATION_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID DOCUMENT_ID = UUID.randomUUID();


    @Test
    void testUploadSingleDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "aadhaar.png", "image/png", "dummy-data".getBytes()
        );

        DocumentResponse response = DocumentResponse.builder()
                .id(DOCUMENT_ID)
                .fileName("aadhaar.png")
                .contentType("image/png")
                .fileSize(BigDecimal.valueOf(1234))
                .documentType(DocumentType.AADHAAR_FRONT)
                .build();

        Mockito.when(documentService.uploadDocument(
                eq(VERIFICATION_ID),
                any(),
                eq(DocumentType.AADHAAR_FRONT),
                any(),
                eq(CUSTOMER_ID)
        )).thenReturn(response);

        mockMvc.perform(multipart("/tenants/1/verification/" + VERIFICATION_ID + "/documents/customer/" + CUSTOMER_ID + "/upload")
                        .file(file)
                        .param("documentTypes", DocumentType.AADHAAR_FRONT.name()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].fileName").value("aadhaar.png"));
    }

    @Test
    void testGetDocuments() throws Exception {
        DocumentResponse response = DocumentResponse.builder()
                .id(DOCUMENT_ID)
                .fileName("doc.pdf")
                .contentType("application/pdf")
                .fileSize(BigDecimal.valueOf(100))
                .build();

        Mockito.when(documentService.getDocuments(VERIFICATION_ID, CUSTOMER_ID))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/tenants/1/verification/" + VERIFICATION_ID + "/documents/customer/" + CUSTOMER_ID + "/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fileName").value("doc.pdf"));
    }

    @Test
    void testDownloadDocument() throws Exception {
        DocumentResponse mockDoc = DocumentResponse.builder()
                .id(DOCUMENT_ID)
                .fileName("test.png")
                .contentType("image/png")
                .fileSize(BigDecimal.valueOf(10))
                .fileData("filecontent".getBytes())
                .build();

        Mockito.when(documentService.getDocumentWithData(DOCUMENT_ID, VERIFICATION_ID, CUSTOMER_ID))
                .thenReturn(mockDoc);

        mockMvc.perform(get("/tenants/1/verification/" + VERIFICATION_ID + "/documents/customer/" + CUSTOMER_ID + "/download/" + DOCUMENT_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=test.png"));
    }


    @Test
    void testUpdateDocument() throws Exception {
        DocumentResponse response = DocumentResponse.builder()
                .id(DOCUMENT_ID)
                .fileName("updated.pdf")
                .documentType(DocumentType.PAN_CARD)
                .build();

        Mockito.when(documentService.updateDocument(eq(DOCUMENT_ID), eq(VERIFICATION_ID), any(), eq(CUSTOMER_ID)))
                .thenReturn(response);

        mockMvc.perform(put("/tenants/1/verification/" + VERIFICATION_ID + "/documents/customer/" + CUSTOMER_ID + "/update/" + DOCUMENT_ID)
                        .param("documentType", DocumentType.PAN_CARD.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("updated.pdf"));
    }

    @Test
    void testUploadFileCountMismatch() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.png", "image/png", "data".getBytes());

        mockMvc.perform(multipart("/tenants/1/verification/" + VERIFICATION_ID + "/documents/customer/" + CUSTOMER_ID + "/upload")
                        .file(file)
                        .param("documentTypes", DocumentType.AADHAAR_FRONT.name(), DocumentType.PAN_CARD.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Number of files must match number of document types"));
    }

}
