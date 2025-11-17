package in.zeta.service;

import in.zeta.entity.Users;

import java.math.BigDecimal;
import java.util.UUID;

public interface OfficerAssignmentService {
    BigDecimal getOfficerWorkload(UUID officerId);
    void assignOfficerToVerification(UUID verificationId);
}
