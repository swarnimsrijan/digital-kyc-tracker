package in.zeta.repository;

import in.zeta.entity.Document;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static in.zeta.enums.DocumentType.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    private Users customer;
    private Users requestor;
    private Users officer;
    private VerificationRequest verificationRequest;

    @BeforeEach
    void setUp() {
        customer = TestDataBuilder.createCustomer();
        requestor = TestDataBuilder.createRequestor();
        officer = TestDataBuilder.createOfficer();

        entityManager.persist(customer);
        entityManager.persist(requestor);
        entityManager.persist(officer);

        verificationRequest = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        entityManager.persist(verificationRequest);
        entityManager.flush();
    }

    @Test
    void testFindByVerificationRequestAndIsActive() {
        // Given
        Document activeDoc = TestDataBuilder.createDocument(verificationRequest, customer);
        Document inactiveDoc = TestDataBuilder.createInactiveDocument(verificationRequest, customer);

        entityManager.persist(activeDoc);
        entityManager.persist(inactiveDoc);
        entityManager.flush();

        // When
        List<Document> activeDocuments =
                documentRepository.findByVerificationRequestAndIsActive(verificationRequest, true);

        // Then
        assertThat(activeDocuments).hasSize(1);
        assertThat(activeDocuments.get(0).getIsActive()).isTrue();
    }

    @Test
    void testFindByVerificationRequestAndUploadedBy() {
        // Given
        Document customerDoc = TestDataBuilder.createDocument(verificationRequest, customer);
        Document officerDoc = TestDataBuilder.createDocumentWithHash(verificationRequest, officer, "officer123hash");
        officerDoc.setFileName("verification_report.pdf");

        entityManager.persist(customerDoc);
        entityManager.persist(officerDoc);
        entityManager.flush();

        // When
        List<Document> customerDocuments =
                documentRepository.findByVerificationRequestAndUploadedBy(verificationRequest, customer);

        // Then
        assertThat(customerDocuments).hasSize(1);
        assertThat(customerDocuments.get(0).getUploadedBy().getId()).isEqualTo(customer.getId());
    }

    @Test
    void testFindByVerificationRequestId() {
        // Given
        Document doc1 = TestDataBuilder.createDocument(verificationRequest, customer);
        Document doc2 = TestDataBuilder.createDocument(verificationRequest, customer);
//        doc2.setDocumentId(UUID.randomUUID());
        doc2.setFileHash("doc2hash123");
        doc2.setFileName("address_proof.pdf");

        Document inactiveDoc = TestDataBuilder.createInactiveDocument(verificationRequest, customer);

        entityManager.persist(doc1);
        entityManager.persist(doc2);
        entityManager.persist(inactiveDoc);
        entityManager.flush();

        // When
        List<Document> documents =
                documentRepository.findByVerificationRequestId(verificationRequest.getId());

        // Then
        assertThat(documents).hasSize(2);
        assertThat(documents).allMatch(Document::getIsActive);
    }

    @Test
    void testFindById() {
        // Given
        Document document = TestDataBuilder.createDocument(verificationRequest, customer);
        entityManager.persist(document);
        entityManager.flush();

        // When
        Optional<Document> foundDocument = documentRepository.findById(document.getId());

        // Then
        assertThat(foundDocument).isPresent();
        assertThat(foundDocument.get().getFileName()).isEqualTo(document.getFileName());
    }

    @Test
    void testFindByFileHashAndVerificationRequest() {
        // Given
        Document document = TestDataBuilder.createDocument(verificationRequest, customer);
        String fileHash = "unique_file_hash_12345";
        document.setFileHash(fileHash);
        entityManager.persist(document);
        entityManager.flush();

        // When
        Optional<Document> foundDocument =
                documentRepository.findByFileHashAndVerificationRequest(fileHash, verificationRequest);

        // Then
        assertThat(foundDocument).isPresent();
        assertThat(foundDocument.get().getFileHash()).isEqualTo(fileHash);
    }

    @Test
    void testFindByVerificationRequestIdAndUploadedById() {
        // Given
        Document doc1 = TestDataBuilder.createDocument(verificationRequest, customer);
        Document doc2 = TestDataBuilder.createDocument(verificationRequest, customer);
//        doc2.se(UUID.randomUUID());
        doc2.setFileHash("doc2hash456");

        entityManager.persist(doc1);
        entityManager.persist(doc2);
        entityManager.flush();

        List<UUID> documentIds = Arrays.asList(doc1.getId(), doc2.getId());

        // When
        List<Document> documents = documentRepository.findByVerificationRequestIdAndUploadedById(
                verificationRequest.getId(),
                customer.getId(),
                documentIds
        );

        // Then
        assertThat(documents).hasSize(2);
        assertThat(documents).allMatch(d -> d.getUploadedBy().getId().equals(customer.getId()));
    }

    @Test
    void testCountActiveDocumentsByVerificationRequestId() {
        // Given
        Document activeDoc1 = TestDataBuilder.createDocument(verificationRequest, customer);
        Document activeDoc2 = TestDataBuilder.createDocument(verificationRequest, customer);
//        activeDoc2.s(UUID.randomUUID());
        activeDoc2.setFileHash("active2hash");

        Document inactiveDoc = TestDataBuilder.createInactiveDocument(verificationRequest, customer);

        entityManager.persist(activeDoc1);
        entityManager.persist(activeDoc2);
        entityManager.persist(inactiveDoc);
        entityManager.flush();

        // When
        long count = documentRepository.countActiveDocumentsByVerificationRequestId(verificationRequest.getId());

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void testFindByUploadedById() {
        // Given
        VerificationRequest vr2 = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        vr2.setId(null);
        entityManager.persist(vr2);

        Document doc1 = TestDataBuilder.createDocument(verificationRequest, customer);
        Document doc2 = TestDataBuilder.createDocument(vr2, customer);
//        doc2.setDocumentId(UUID.randomUUID());
        doc2.setFileHash("doc2different");

        Document inactiveDoc = TestDataBuilder.createInactiveDocument(verificationRequest, customer);

        entityManager.persist(doc1);
        entityManager.persist(doc2);
        entityManager.persist(inactiveDoc);
        entityManager.flush();

        // When
        List<Document> customerDocuments = documentRepository.findByUploadedById(customer.getId());

        // Then
        assertThat(customerDocuments).hasSize(2);
        assertThat(customerDocuments).allMatch(Document::getIsActive);
    }

    @Test
    void testFindByVerificationRequestIdAndUploadedById_WithWrongUser() {
        // Given
        Document doc = TestDataBuilder.createDocument(verificationRequest, customer);
        entityManager.persist(doc);
        entityManager.flush();

        // When
        List<Document> documents = documentRepository.findByVerificationRequestIdAndUploadedById(
                verificationRequest.getId(),
                officer.getId(), // Different user
                Arrays.asList(doc.getId())
        );

        // Then
        assertThat(documents).isEmpty();
    }

    @Test
    void testFindByFileHashAndVerificationRequest_NotFound() {
        // When
        Optional<Document> document =
                documentRepository.findByFileHashAndVerificationRequest("nonexistent_hash", verificationRequest);

        // Then
        assertThat(document).isEmpty();
    }

    @Test
    void testDocumentTypes() {
        // Given
        Document addressProof = TestDataBuilder.createDocument(verificationRequest, customer);
        addressProof.setDocumentType(ADDRESS_PROOF);

        Document aadhaarFront = TestDataBuilder.createDocument(verificationRequest, customer);
        aadhaarFront.setDocumentType(AADHAAR_FRONT);
        aadhaarFront.setFileHash("aadhaarFront123");
        aadhaarFront.setFileName("aadhaar_front.pdf");

        Document aadhaarBack = TestDataBuilder.createDocument(verificationRequest, customer);
        aadhaarBack.setDocumentType(AADHAAR_BACK);
        aadhaarBack.setFileHash("aadhaarBack123");
        aadhaarBack.setFileName("aadhaar_back.pdf");

        entityManager.persist(addressProof);
        entityManager.persist(aadhaarFront);
        entityManager.persist(aadhaarBack);
        entityManager.flush();

        // When
        List<Document> documents = documentRepository.findByVerificationRequestId(verificationRequest.getId());

        // Then
        assertThat(documents).hasSize(3);
        assertThat(documents).extracting(Document::getDocumentType)
                .containsExactlyInAnyOrder(ADDRESS_PROOF, AADHAAR_FRONT, AADHAAR_BACK);
    }
}