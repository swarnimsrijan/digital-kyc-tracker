package in.zeta.providers;

import in.zeta.oms.sandbox.model.object.ObjectProvider;
import in.zeta.oms.sandbox.model.realm.Realm;
import in.zeta.dto.response.DocumentResponse;
import in.zeta.service.DocumentService;
import in.zeta.spectra.capture.SpectraLogger;
import olympus.common.JID;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class DocumentsProvider implements ObjectProvider<DocumentResponse> {
    public static final String OBJECT_TYPE = "kycsw_document";
    private final DocumentService documentService;
    private static final SpectraLogger logger = OlympusSpectra.getLogger(DocumentsProvider.class);

    @Autowired
    public DocumentsProvider(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public CompletionStage<Optional<DocumentResponse>> getObject(JID jid, Realm realm, Long tenantID) {
        return CompletableFuture.supplyAsync(() -> {
            DocumentResponse documentResponse = null;
            UUID documentId = null;

            try {
                documentId = UUID.fromString(jid.getNodeId());

                logger.info("Entry: Fetching document object")
                        .attr("document id", documentId)
                        .log();

                documentResponse = documentService.getDocumentById(documentId);

                if (documentResponse == null) {
                    logger.error("document not found")
                            .attr("document id", documentId)
                            .log();

                    documentResponse = getDefaultDocument();
                } else {
                    logger.info("Success: Document object fetched")
                            .attr("document id", documentId)
                            .log();
                }

            } catch (Exception ex) {
                logger.error("Error fetching document object")
                        .attr("document id", documentId)
                        .attr("error", ex.getMessage())
                        .log();

                documentResponse = getDefaultDocument();
            }

            return Optional.of(documentResponse);
        });
    }

    private DocumentResponse getDefaultDocument() {
        return new DocumentResponse();
    }
}