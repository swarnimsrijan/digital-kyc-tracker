package in.zeta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_request_limits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRequestLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Users customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_requestor", nullable = false)
    private Users verificationRequestor;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "request_count_by_requestor")
    private Integer requestCount;

    @Column(name = "total_requests")
    private Integer totalRequests;

    @Column(name = "max_allowed_requests", nullable = false)
    private Integer maxAllowedRequests;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}