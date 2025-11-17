package in.zeta.entity;


import in.zeta.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "verification_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Users customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id", nullable = false)
    private Users requestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private Users assignedOfficer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @OneToMany(mappedBy = "verificationRequest", cascade = CascadeType.ALL)
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "verificationRequest", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "verificationRequest", cascade = CascadeType.ALL)
    private List<StatusHistory> statusHistories = new ArrayList<>();

}