package in.zeta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.dto.response.RequestCountResponse;
import in.zeta.dto.response.RequestorCountListResponse;
import in.zeta.service.VerificationRequestLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VerificationRequestLimitController.class)
@TestPropertySource(properties = {
    "spring.cloud.sleuth.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class VerificationRequestLimitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VerificationRequestLimitService verificationRequestLimitService;

    private UUID requestorId;
    private UUID customerId;
    private int currentYear;
    private RequestCountResponse mockRequestCountResponse;
    private List<RequestorCountListResponse> mockRequestorCountList;

    @BeforeEach
    void setUp() {
        requestorId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        currentYear = LocalDate.now().getYear();

        mockRequestCountResponse = createMockRequestCountResponse();
        mockRequestorCountList = createMockRequestorCountList();
    }

    private RequestCountResponse createMockRequestCountResponse() {
        return RequestCountResponse.builder()
                .customerId(customerId)
                .year(currentYear)
                .requestCount(5)
                .maxAllowedRequests(10)
                .build();
    }

    private List<RequestorCountListResponse> createMockRequestorCountList() {
        List<RequestorCountListResponse> list = new ArrayList<>();

        RequestorCountListResponse response1 = RequestorCountListResponse.builder()
                .requestorId(UUID.randomUUID())
                .requestorName("Requestor 1")
                .requestCount(3)
                .maxAllowedRequests(10)
                .year(currentYear)
                .build();

        RequestorCountListResponse response2 = RequestorCountListResponse.builder()
                .requestorId(UUID.randomUUID())
                .requestorName("Requestor 2")
                .requestCount(5)
                .maxAllowedRequests(10)
                .year(currentYear)
                .build();

        list.add(response1);
        list.add(response2);
        return list;
    }

    @Test
    void getRequestorRequestsToCustomerCurrentYear_ShouldReturnZeroCount_WhenNoDataExists() throws Exception {
        // Arrange
        RequestCountResponse emptyResponse = RequestCountResponse.builder()
                .customerId(customerId)
                .year(currentYear)
                .requestCount(0)
                .maxAllowedRequests(10)
                .build();

        when(verificationRequestLimitService.getRequestorRequestsToCustomerCurrentYear(
                any(UUID.class), any(UUID.class), anyInt()))
                .thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/requestor/{requestorId}/customer/{customerId}/year/{year}",
                        requestorId, customerId, currentYear)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount").value(0))
                .andExpect(jsonPath("$.maxAllowedRequests").value(10));

        verify(verificationRequestLimitService, times(1))
                .getRequestorRequestsToCustomerCurrentYear(requestorId, customerId, currentYear);
    }

    @Test
    void getRequestorRequestsToCustomerCurrentYear_ShouldWorkWithDifferentYears() throws Exception {
        // Arrange
        int pastYear = 2023;
        RequestCountResponse pastYearResponse = RequestCountResponse.builder()
                .customerId(customerId)
                .year(pastYear)
                .requestCount(8)
                .maxAllowedRequests(10)
                .build();

        when(verificationRequestLimitService.getRequestorRequestsToCustomerCurrentYear(
                requestorId, customerId, pastYear))
                .thenReturn(pastYearResponse);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/requestor/{requestorId}/customer/{customerId}/year/{year}",
                        requestorId, customerId, pastYear)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(pastYear))
                .andExpect(jsonPath("$.requestCount").value(8));

        verify(verificationRequestLimitService, times(1))
                .getRequestorRequestsToCustomerCurrentYear(requestorId, customerId, pastYear);
    }

    @Test
    void getTotalRequestsToCustomerCurrentYear_ShouldReturnTotalCount_WhenDataExists() throws Exception {
        // Arrange
        RequestCountResponse totalResponse = RequestCountResponse.builder()
                .customerId(customerId)
                .year(currentYear)
                .requestCount(15)
                .build();

        when(verificationRequestLimitService.getTotalRequestsToCustomerCurrentYear(any(UUID.class)))
                .thenReturn(totalResponse);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/total-requests/current-year",
                        customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.year").value(currentYear))
                .andExpect(jsonPath("$.requestCount").value(15));

        verify(verificationRequestLimitService, times(1))
                .getTotalRequestsToCustomerCurrentYear(customerId);
    }

    @Test
    void getTotalRequestsToCustomerCurrentYear_ShouldReturnZero_WhenNoRequestsExist() throws Exception {
        // Arrange
        RequestCountResponse emptyResponse = RequestCountResponse.builder()
                .customerId(customerId)
                .year(currentYear)
                .requestCount(0)
                .build();

        when(verificationRequestLimitService.getTotalRequestsToCustomerCurrentYear(any(UUID.class)))
                .thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/total-requests/current-year",
                        customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount").value(0));

        verify(verificationRequestLimitService, times(1))
                .getTotalRequestsToCustomerCurrentYear(customerId);
    }

    @Test
    void getTotalRequestsToCustomerCurrentYear_ShouldWorkWithDifferentCustomers() throws Exception {
        // Arrange
        UUID customer1 = UUID.randomUUID();
        UUID customer2 = UUID.randomUUID();

        RequestCountResponse response1 = RequestCountResponse.builder()
                .customerId(customer1)
                .year(currentYear)
                .requestCount(10)
                .build();

        RequestCountResponse response2 = RequestCountResponse.builder()
                .customerId(customer2)
                .year(currentYear)
                .requestCount(5)
                .build();

        when(verificationRequestLimitService.getTotalRequestsToCustomerCurrentYear(customer1))
                .thenReturn(response1);
        when(verificationRequestLimitService.getTotalRequestsToCustomerCurrentYear(customer2))
                .thenReturn(response2);

        // Act & Assert - Customer 1
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/total-requests/current-year",
                        customer1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customer1.toString()))
                .andExpect(jsonPath("$.requestCount").value(10));

        // Act & Assert - Customer 2
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/total-requests/current-year",
                        customer2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customer2.toString()))
                .andExpect(jsonPath("$.requestCount").value(5));

        verify(verificationRequestLimitService, times(1)).getTotalRequestsToCustomerCurrentYear(customer1);
        verify(verificationRequestLimitService, times(1)).getTotalRequestsToCustomerCurrentYear(customer2);
    }

    @Test
    void getAllRequestorsCountForCustomer_ShouldReturnList_WhenRequestorsExist() throws Exception {
        // Arrange
        when(verificationRequestLimitService.getAllRequestorsCountForCustomer(any(UUID.class)))
                .thenReturn(mockRequestorCountList);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/requestors-count",
                        customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].requestorName").value("Requestor 1"))
                .andExpect(jsonPath("$[0].requestCount").value(3))
                .andExpect(jsonPath("$[0].maxAllowedRequests").value(10))
                .andExpect(jsonPath("$[0].year").value(currentYear))
                .andExpect(jsonPath("$[1].requestorName").value("Requestor 2"))
                .andExpect(jsonPath("$[1].requestCount").value(5));

        verify(verificationRequestLimitService, times(1))
                .getAllRequestorsCountForCustomer(customerId);
    }

    @Test
    void getAllRequestorsCountForCustomer_ShouldReturnEmptyList_WhenNoRequestorsExist() throws Exception {
        // Arrange
        when(verificationRequestLimitService.getAllRequestorsCountForCustomer(any(UUID.class)))
                .thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/requestors-count",
                        customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(verificationRequestLimitService, times(1))
                .getAllRequestorsCountForCustomer(customerId);
    }

    @Test
    void getAllRequestorsCountForCustomer_ShouldReturnMultipleRequestors() throws Exception {
        // Arrange
        List<RequestorCountListResponse> largeList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            largeList.add(RequestorCountListResponse.builder()
                    .requestorId(UUID.randomUUID())
                    .requestorName("Requestor " + i)
                    .requestCount(i)
                    .maxAllowedRequests(10)
                    .year(currentYear)
                    .build());
        }

        when(verificationRequestLimitService.getAllRequestorsCountForCustomer(any(UUID.class)))
                .thenReturn(largeList);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/requestors-count",
                        customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].requestorName").value("Requestor 1"))
                .andExpect(jsonPath("$[4].requestorName").value("Requestor 5"));

        verify(verificationRequestLimitService, times(1))
                .getAllRequestorsCountForCustomer(customerId);
    }

    @Test
    void allEndpoints_ShouldReturnCorrectContentType() throws Exception {
        // Arrange
        when(verificationRequestLimitService.getRequestorRequestsToCustomerCurrentYear(
                any(), any(), anyInt())).thenReturn(mockRequestCountResponse);
        when(verificationRequestLimitService.getTotalRequestsToCustomerCurrentYear(any()))
                .thenReturn(mockRequestCountResponse);
        when(verificationRequestLimitService.getAllRequestorsCountForCustomer(any()))
                .thenReturn(mockRequestorCountList);

        // Act & Assert - getRequestorRequestsToCustomerCurrentYear
        mockMvc.perform(get("/verification-request-limits/requestor/{requestorId}/customer/{customerId}/year/{year}",
                        requestorId, customerId, currentYear))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Act & Assert - getTotalRequestsToCustomerCurrentYear
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/total-requests/current-year",
                        customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Act & Assert - getAllRequestorsCountForCustomer
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/requestors-count",
                        customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getRequestorRequestsToCustomerCurrentYear_ShouldHandleMaxLimitReached() throws Exception {
        // Arrange
        RequestCountResponse maxLimitResponse = RequestCountResponse.builder()
                .customerId(customerId)
                .year(currentYear)
                .requestCount(10)
                .maxAllowedRequests(10)
                .build();

        when(verificationRequestLimitService.getRequestorRequestsToCustomerCurrentYear(
                any(UUID.class), any(UUID.class), anyInt()))
                .thenReturn(maxLimitResponse);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/requestor/{requestorId}/customer/{customerId}/year/{year}",
                        requestorId, customerId, currentYear))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount").value(10))
                .andExpect(jsonPath("$.maxAllowedRequests").value(10));

        verify(verificationRequestLimitService, times(1))
                .getRequestorRequestsToCustomerCurrentYear(requestorId, customerId, currentYear);
    }

    @Test
    void getAllRequestorsCountForCustomer_ShouldHandleRequestorsWithDifferentLimits() throws Exception {
        // Arrange
        List<RequestorCountListResponse> mixedLimitsList = new ArrayList<>();

        mixedLimitsList.add(RequestorCountListResponse.builder()
                .requestorId(UUID.randomUUID())
                .requestorName("Premium Requestor")
                .requestCount(8)
                .maxAllowedRequests(20)
                .year(currentYear)
                .build());

        mixedLimitsList.add(RequestorCountListResponse.builder()
                .requestorId(UUID.randomUUID())
                .requestorName("Standard Requestor")
                .requestCount(5)
                .maxAllowedRequests(10)
                .year(currentYear)
                .build());

        when(verificationRequestLimitService.getAllRequestorsCountForCustomer(any(UUID.class)))
                .thenReturn(mixedLimitsList);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/customer/{customerId}/requestors-count",
                        customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestorName").value("Premium Requestor"))
                .andExpect(jsonPath("$[0].maxAllowedRequests").value(20))
                .andExpect(jsonPath("$[1].requestorName").value("Standard Requestor"))
                .andExpect(jsonPath("$[1].maxAllowedRequests").value(10));

        verify(verificationRequestLimitService, times(1))
                .getAllRequestorsCountForCustomer(customerId);
    }

    @Test
    void getRequestorRequestsToCustomerCurrentYear_ShouldReturnRequestCount_WhenDataExists() throws Exception {
        // Arrange
        when(verificationRequestLimitService.getRequestorRequestsToCustomerCurrentYear(
                any(UUID.class), any(UUID.class), anyInt()))
                .thenReturn(mockRequestCountResponse);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/requestor/{requestorId}/customer/{customerId}/year/{year}",
                        requestorId, customerId, currentYear)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.year").value(currentYear))
                .andExpect(jsonPath("$.requestCount").value(5))
                .andExpect(jsonPath("$.maxAllowedRequests").value(10));

        verify(verificationRequestLimitService, times(1))
                .getRequestorRequestsToCustomerCurrentYear(requestorId, customerId, currentYear);
    }

    @Test
    void getRequestorRequestsToCustomerCurrentYear_ShouldHandleValidUUIDs() throws Exception {
        // Arrange
        UUID specificRequestorId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID specificCustomerId = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

        RequestCountResponse response = RequestCountResponse.builder()
                .customerId(specificCustomerId)
                .year(currentYear)
                .requestCount(3)
                .maxAllowedRequests(10)
                .build();

        when(verificationRequestLimitService.getRequestorRequestsToCustomerCurrentYear(
                specificRequestorId, specificCustomerId, currentYear))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/verification-request-limits/requestor/{requestorId}/customer/{customerId}/year/{year}",
                        specificRequestorId, specificCustomerId, currentYear))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(specificCustomerId.toString()))
                .andExpect(jsonPath("$.year").value(currentYear))
                .andExpect(jsonPath("$.requestCount").value(3))
                .andExpect(jsonPath("$.maxAllowedRequests").value(10));

        verify(verificationRequestLimitService, times(1))
                .getRequestorRequestsToCustomerCurrentYear(specificRequestorId, specificCustomerId, currentYear);
    }
}