package in.zeta.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestorCountListResponse {
    private UUID requestorId;
    private UUID customerId;
    private String requestorName;
    private String requestorEmail;
    private Integer requestCount;
    private Integer maxAllowedRequests;
    private Integer year;
}