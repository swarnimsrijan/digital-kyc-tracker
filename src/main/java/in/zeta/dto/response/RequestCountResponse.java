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
public class RequestCountResponse {
    private UUID customerId;
    private Integer requestCount;
    private Integer maxAllowedRequests;
    private Integer year;
    private String customerName;
}