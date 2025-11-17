package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.enums.DocumentType;
import in.zeta.providers.DocumentsProvider;
import in.zeta.dto.requests.DocumentUpdateRequest;
import in.zeta.dto.response.ApiResponse;
import in.zeta.dto.response.DocumentMetadataResponse;
import in.zeta.dto.response.DocumentResponse;
import in.zeta.service.DocumentService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAuthorizedSync;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
import static in.zeta.constants.Messages.*;

@RestController
@RequestMapping("/tenants/{tenantId}/verification/{verificationId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SpectraLogger logger = OlympusSpectra.getLogger(DocumentController.class);

    @PostMapping(value = "/customer/{customerId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SandboxAuthorizedSync(action = "document.create", object = "$$customerId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> uploadDocuments(
            @PathVariable("verificationId") UUID verificationId,
            @PathVariable("customerId") UUID customerId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("documentTypes") DocumentType[] documentTypes,
            @RequestParam(value = "descriptions", required = false) String[] descriptions) {

        logger.info(Messages.Document.UPLOADING_DOCUMENT)
                .attr(Messages.Keys.VERIFICATION_ID, verificationId)
                .attr(Messages.Keys.CUSTOMER_ID, customerId)
                .log();

        if (files.length == 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(Messages.Errors.FILE_EXPECTED));
        }

        if (files.length != documentTypes.length) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Number of files must match number of document types"));
        }

        if (descriptions != null && descriptions.length != files.length) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Number of files must match number of document types"));
        }

        List<DocumentResponse> responses;

        // Handle single file upload
        if (files.length == 1) {
            logger.info("Processing single document upload")
                    .attr("verificationId", verificationId)
                    .attr("customerId", customerId)
                    .attr("documentType", documentTypes[0])
                    .log();

            DocumentResponse response = documentService.uploadDocument(
                    verificationId,
                    files[0],
                    documentTypes[0],
                    descriptions != null ? descriptions[0] : null,
                    customerId
            );
            responses = List.of(response);
        }
        else {
            logger.info("Processing batch document upload")
                    .attr("verificationId", verificationId)
                    .attr("customerId", customerId)
                    .attr("fileCount", files.length)
                    .log();

            responses = documentService.uploadMultipleDocuments(
                    verificationId,
                    files,
                    documentTypes,
                    descriptions,
                    customerId
            );
        }

        String message = files.length == 1 ?
                Document.UPLOAD_SUCCESS :
                String.format("%d " + Document.UPLOAD_SUCCESS, files.length);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, responses));
    }


    @GetMapping("/customer/{customerId}/download/{documentId}")
    @SandboxAuthorizedSync(action = "document.read", object = "$$documentId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable("verificationId") UUID verificationId,
            @PathVariable("documentId") UUID documentId,
            @PathVariable("customerId") UUID customerId) {

        logger.info("Processing document download")
                .attr("verificationId", verificationId)
                .attr("customerId", customerId)
                .attr("documentId", documentId)
                .log();


        try {
            DocumentResponse document = documentService.getDocumentWithData(documentId, verificationId, customerId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getContentType()));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getFileName());
            headers.setContentLength(document.getFileSize().longValue());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(document.getFileData());

        } catch (Exception e) {
            logger.error("Failed to download document")
                    .attr("customerId", customerId)
                    .attr("verificationId", verificationId)
                    .attr("documentId", documentId)
                    .log();
            throw e;
        }
    }


    @PostMapping("/customer/{customerId}/download/batch")
    @SandboxAuthorizedSync(action = "document.read", object = "$$customerId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<byte[]> downloadMultipleDocuments(
            @PathVariable("verificationId") UUID verificationId,
            @RequestBody List<UUID> documentIds,
            @PathVariable("customerId") UUID customerId) {

        byte[] zipData = documentService.downloadMultipleDocuments(documentIds, verificationId, customerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "documents_" + verificationId + ".zip");

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipData);
    }


    @GetMapping("/customer/{customerId}/download/all")
    @SandboxAuthorizedSync(action = "document.read", object = "$$customerId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<byte[]> downloadAllDocuments(
            @PathVariable("verificationId") UUID verificationId,
            @PathVariable("customerId") UUID customerId) {


        byte[] zipData = documentService.downloadAllDocuments(verificationId, customerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "all_documents_" + verificationId + ".zip");

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipData);
    }

    @PutMapping("/customer/{customerId}/update/{documentId}")
    @SandboxAuthorizedSync(action = "document.update", object = "$$customerId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateSingleDocument(
            @PathVariable("verificationId") UUID verificationId,
            @PathVariable("documentId") UUID documentId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "documentType", required = false) DocumentType documentType,
            @RequestParam(value = "description", required = false) String description,
            @PathVariable("customerId") UUID customerId) {

        DocumentUpdateRequest updateRequest = DocumentUpdateRequest.builder()
                .file(file)
                .documentType(documentType)
                .description(description)
                .build();

        DocumentResponse response = documentService
                .updateDocument(documentId, verificationId, updateRequest, customerId);

        return ResponseEntity.ok(ApiResponse.success("Document updated successfully", response));
    }


    @PutMapping("/customer/{customerId}/update/batch")
    @SandboxAuthorizedSync(action = "document.update", object = "$$customerId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> updateMultipleDocuments(
            @PathVariable("verificationId") UUID verificationId,
            @RequestParam("documentIds") List<UUID> documentIds,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "documentTypes", required = false) DocumentType[] documentTypes,
            @RequestParam(value = "descriptions", required = false) String[] descriptions,
            @PathVariable("customerId") UUID customerId) {



        List<DocumentResponse> responses = documentService
                .updateMultipleDocuments(verificationId, documentIds, files, documentTypes, descriptions, customerId);

        return ResponseEntity.ok(ApiResponse.success("Documents updated successfully", responses));
    }

    @GetMapping("/customer/{customerId}/metadata")
    @SandboxAuthorizedSync(action = "document.read", object = "$$customerId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<DocumentMetadataResponse>>> getDocumentsMetadata(
            @PathVariable("verificationId") UUID verificationId,
            @PathVariable("customerId") UUID customerId){

        List<DocumentMetadataResponse> metadata = documentService
                .getDocumentsMetadata(verificationId, customerId);

        return ResponseEntity.ok(ApiResponse.success(metadata));
    }

    @GetMapping("/metadata/{documentId}")
    @SandboxAuthorizedSync(action = "document.read", object = "$$documentId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<DocumentMetadataResponse>> getDocumentMetadata(
            @PathVariable("verificationId") UUID verificationId,
            @PathVariable("documentId") UUID documentId) {

        logger.info("Fetching document metadata")
                .attr("verificationId", verificationId)
                .attr("documentId", documentId)
                .log();

        DocumentMetadataResponse metadata = documentService
                .getDocumentMetadata(documentId, verificationId);

        logger.info("Fetched document metadata successfully")
                .attr("verificationId", verificationId)
                .attr("documentId", documentId)
                .log();

        return ResponseEntity.ok(ApiResponse.success(metadata));
    }

    @DeleteMapping("/customer/{customerId}/delete/{documentId}")
    @SandboxAuthorizedSync(action = "document.delete", object = "$$documentId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable("verificationId") UUID verificationId,
            @PathVariable("documentId") UUID documentId,
            @PathVariable("customerId") UUID customerId){

        logger.info("Processing document deletion")
                .attr("verificationId", verificationId)
                .attr("customerId", customerId)
                .attr("documentId", documentId)
                .log();

        documentService.deleteDocument(documentId, verificationId, customerId);

        logger.info("Document deleted successfully")
                .attr("verificationId", verificationId)
                .attr("customerId", customerId)
                .attr("documentId", documentId)
                .log();

        return ResponseEntity.ok(ApiResponse.success(Document.DELETE_SUCCESS, null));
    }

    @DeleteMapping("/customer/{customerId}/delete/batch")
    @SandboxAuthorizedSync(action = "document.delete", object = "$$customerId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleDocuments(
            @PathVariable("verificationId") UUID verificationId,
            @RequestBody List<UUID> documentIds,
            @PathVariable("customerId") UUID customerId){

        logger.info("Deleting multiple documents")
                .attr("verificationId", verificationId)
                .attr("customerId", customerId)
                .attr("documentCount", documentIds.size())
                .log();

        documentService.deleteMultipleDocuments(verificationId, customerId, documentIds);

        logger.info("Documents deleted successfully")
                .attr("verificationId", verificationId)
                .attr("customerId", customerId)
                .log();

        return ResponseEntity.ok(ApiResponse.success(Document.DELETE_SUCCESS, null));
    }

    @GetMapping("/customer/{customerId}/view")
    @SandboxAuthorizedSync(action = "document.read", object = "$$customerId$$@" + DocumentsProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("verificationId") UUID verificationId) {

        logger.info("Fetching documents for verification")
                .attr("verificationId", verificationId)
                .attr("customerId", customerId)
                .log();

        List<DocumentResponse> responses = documentService.getDocuments(verificationId, customerId);

        logger.info(Document.FETCH_LIST_SUCCESS)
                .attr("verificationId", verificationId)
                .attr("customerId", customerId)
                .attr("documentCount", responses.size())
                .log();

        return ResponseEntity.ok(ApiResponse.success(Document.FETCH_LIST_SUCCESS, responses));
    }
}