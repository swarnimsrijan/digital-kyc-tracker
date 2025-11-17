package in.zeta.service.impl;

import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequestLimit;
import in.zeta.enums.Role;
import in.zeta.exception.DataNotFoundException;
import in.zeta.repository.VerificationRequestLimitRepository;
import in.zeta.dto.response.RequestCountResponse;
import in.zeta.dto.response.RequestorCountListResponse;
import in.zeta.service.UserService;
import in.zeta.spectra.capture.SpectraLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationRequestLimitServiceImplTest {

    @Mock
    private VerificationRequestLimitRepository verificationRequestLimitRepository;

    @Mock
    private UserService userService;

    @Mock
    private SpectraLogger logger;

    @InjectMocks
    private VerificationRequestLimitServiceImpl verificationRequestLimitService;

    private UUID testCustomerId;
    private UUID testRequestorId;
    private Users testCustomer;
    private Users testRequestor;
    private VerificationRequestLimit testLimit;
    private int currentYear;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();
        testRequestorId = UUID.randomUUID();
        currentYear = LocalDate.now().getYear();

        testCustomer = Users.builder()
                .id(testCustomerId)
                .email("customer@example.com")
                .username("Customer")
                .role(Role.CUSTOMER)
                .build();

        testRequestor = Users.builder()
                .id(testRequestorId)
                .email("requestor@example.com")
                .username("Test Requestor")
                .role(Role.VERIFICATION_REQUESTOR)
                .build();

        testLimit = VerificationRequestLimit.builder()
                .id(UUID.randomUUID())
                .customer(testCustomer)
                .verificationRequestor(testRequestor)
                .year(currentYear)
                .requestCount(5)
                .totalRequests(5)
                .maxAllowedRequests(50)
                .createdAt(LocalDateTime.now())
                .build();

        ReflectionTestUtils.setField(verificationRequestLimitService, "defaultMaxAllowedRequests", 10);
        ReflectionTestUtils.setField(verificationRequestLimitService, "maxVerificationRequestsToEachCustomer", 50);
    }

    @Test
    void canCreateVerificationRequest_Success_NoExistingRecord() {
        // Given
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.empty());

        // When
        boolean result = verificationRequestLimitService.canCreateVerificationRequest(testCustomer, testRequestor);

        // Then
        assertTrue(result);
        verify(verificationRequestLimitRepository).findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear);
    }

    @Test
    void canCreateVerificationRequest_Success_WithinLimits() {
        // Given
        testLimit.setRequestCount(5);
        testLimit.setTotalRequests(20);
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.of(testLimit));

        // When
        boolean result = verificationRequestLimitService.canCreateVerificationRequest(testCustomer, testRequestor);

        // Then
        assertTrue(result);
    }

    @Test
    void canCreateVerificationRequest_False_ExceededRequestorLimit() {
        // Given
        testLimit.setRequestCount(10);
        testLimit.setTotalRequests(20);
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.of(testLimit));

        // When
        boolean result = verificationRequestLimitService.canCreateVerificationRequest(testCustomer, testRequestor);

        // Then
        assertFalse(result);
    }

    @Test
    void canCreateVerificationRequest_False_ExceededTotalLimit() {
        // Given
        testLimit.setRequestCount(5);
        testLimit.setTotalRequests(50);
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.of(testLimit));

        // When
        boolean result = verificationRequestLimitService.canCreateVerificationRequest(testCustomer, testRequestor);

        // Then
        assertFalse(result);
    }

    @Test
    void incrementRequestCount_ExistingRecord_WithinLimit() {
        // Given
        testLimit.setRequestCount(5);
        testLimit.setTotalRequests(20);
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.of(testLimit));
        when(verificationRequestLimitRepository.save(any(VerificationRequestLimit.class))).thenReturn(testLimit);

        // When
        verificationRequestLimitService.incrementRequestCount(testCustomer, testRequestor);

        // Then
        assertEquals(6, testLimit.getRequestCount());
        assertEquals(21, testLimit.getTotalRequests());
        verify(verificationRequestLimitRepository).save(testLimit);
    }

    @Test
    void incrementRequestCount_ExistingRecord_ExceededLimit_ResetsCount() {
        // Given
        testLimit.setRequestCount(15);
        testLimit.setTotalRequests(30);
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.of(testLimit));
        when(verificationRequestLimitRepository.save(any(VerificationRequestLimit.class))).thenReturn(testLimit);

        // When
        verificationRequestLimitService.incrementRequestCount(testCustomer, testRequestor);

        // Then
        assertEquals(1, testLimit.getRequestCount());
        assertEquals(31, testLimit.getTotalRequests());
        verify(verificationRequestLimitRepository).save(testLimit);
    }

    @Test
    void incrementRequestCount_NewRecord() {
        // Given
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.empty());
        when(verificationRequestLimitRepository.save(any(VerificationRequestLimit.class))).thenReturn(testLimit);

        // When
        verificationRequestLimitService.incrementRequestCount(testCustomer, testRequestor);

        // Then
        verify(verificationRequestLimitRepository).save(any(VerificationRequestLimit.class));
    }

    @Test
    void getRequestorRequestsToCustomerCurrentYear_ExistingRecord() {
        // Given
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.of(testLimit));

        // When
        RequestCountResponse result = verificationRequestLimitService.getRequestorRequestsToCustomerCurrentYear(
                testRequestorId, testCustomerId, currentYear);

        // Then
        assertNotNull(result);
        assertEquals(testCustomerId, result.getCustomerId());
        assertEquals(currentYear, result.getYear());
        assertEquals(5, result.getRequestCount());
        assertEquals(50, result.getMaxAllowedRequests());
    }

    @Test
    void getRequestorRequestsToCustomerCurrentYear_NoRecord_ThrowsException() {
        // Given
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () ->
                verificationRequestLimitService.getRequestorRequestsToCustomerCurrentYear(
                        testRequestorId, testCustomerId, currentYear));
    }

    @Test
    void getTotalRequestsToCustomerCurrentYear_Success() {
        // Given
        when(userService.getByIdOrThrow(testCustomerId, "Customer not found")).thenReturn(testCustomer);
        when(verificationRequestLimitRepository.getTotalRequestsForCustomerInYear(testCustomerId, currentYear))
                .thenReturn(25);

        // When
        RequestCountResponse result = verificationRequestLimitService.getTotalRequestsToCustomerCurrentYear(testCustomerId);

        // Then
        assertNotNull(result);
        assertEquals(testCustomerId, result.getCustomerId());
        assertEquals(currentYear, result.getYear());
        assertEquals("Customer", result.getCustomerName());
        assertEquals(25, result.getRequestCount());
        assertEquals(10, result.getMaxAllowedRequests());
    }

    @Test
    void getTotalRequestsToCustomerCurrentYear_NullResult_ThrowsException() {
        // Given
        when(userService.getByIdOrThrow(testCustomerId, "Customer not found")).thenReturn(testCustomer);
        when(verificationRequestLimitRepository.getTotalRequestsForCustomerInYear(testCustomerId, currentYear))
                .thenReturn(null);

        // When & Then
        assertThrows(DataNotFoundException.class, () ->
                verificationRequestLimitService.getTotalRequestsToCustomerCurrentYear(testCustomerId));
    }

    @Test
    void getTotalRequestsToCustomerCurrentYear_ZeroResult_ThrowsException() {
        // Given
        when(userService.getByIdOrThrow(testCustomerId, "Customer not found")).thenReturn(testCustomer);
        when(verificationRequestLimitRepository.getTotalRequestsForCustomerInYear(testCustomerId, currentYear))
                .thenReturn(0);

        // When & Then
        assertThrows(DataNotFoundException.class, () ->
                verificationRequestLimitService.getTotalRequestsToCustomerCurrentYear(testCustomerId));
    }

    @Test
    void getAllRequestorsCountForCustomer_Success() {
        // Given
        List<VerificationRequestLimit> limits = Arrays.asList(testLimit);
        when(verificationRequestLimitRepository.findByCustomerIdAndYear(testCustomerId, currentYear))
                .thenReturn(limits);

        // When
        List<RequestorCountListResponse> result = verificationRequestLimitService
                .getAllRequestorsCountForCustomer(testCustomerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(verificationRequestLimitRepository).findByCustomerIdAndYear(testCustomerId, currentYear);
    }

    @Test
    void getAllRequestorsCountForCustomer_EmptyList_ThrowsException() {
        // Given
        when(verificationRequestLimitRepository.findByCustomerIdAndYear(testCustomerId, currentYear))
                .thenReturn(Arrays.asList());

        // When & Then
        assertThrows(DataNotFoundException.class, () ->
                verificationRequestLimitService.getAllRequestorsCountForCustomer(testCustomerId));
    }

    @Test
    void getAllRequestorsCountForCustomer_NullResult_ThrowsException() {
        // Given
        when(verificationRequestLimitRepository.findByCustomerIdAndYear(testCustomerId, currentYear))
                .thenReturn(null);

        // When & Then
        assertThrows(DataNotFoundException.class, () ->
                verificationRequestLimitService.getAllRequestorsCountForCustomer(testCustomerId));
    }

    @Test
    void canCreateVerificationRequest_HandlesNullValues() {
        // Given
        testLimit.setRequestCount(null);
        testLimit.setTotalRequests(null);
        testLimit.setMaxAllowedRequests(null);
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.of(testLimit));

        // When
        boolean result = verificationRequestLimitService.canCreateVerificationRequest(testCustomer, testRequestor);

        // Then
        assertTrue(result);
    }

    @Test
    void incrementRequestCount_HandlesNullValues() {
        // Given
        testLimit.setRequestCount(null);
        testLimit.setTotalRequests(null);
        when(verificationRequestLimitRepository.findByCustomerIdAndVerificationRequestorIdAndYear(
                testCustomerId, testRequestorId, currentYear)).thenReturn(Optional.of(testLimit));
        when(verificationRequestLimitRepository.save(any(VerificationRequestLimit.class))).thenReturn(testLimit);

        // When
        verificationRequestLimitService.incrementRequestCount(testCustomer, testRequestor);

        // Then
        assertEquals(1, testLimit.getRequestCount());
        assertEquals(1, testLimit.getTotalRequests());
        verify(verificationRequestLimitRepository).save(testLimit);
    }
}