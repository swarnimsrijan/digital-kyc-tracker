package in.zeta.dto.requests;

import in.zeta.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoleRequest {
    private String email;
    private Role role;
}
