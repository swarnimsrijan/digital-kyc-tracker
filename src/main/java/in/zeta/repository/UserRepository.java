package in.zeta.repository;


import in.zeta.entity.Users;
import in.zeta.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    List<Users> findByRole(Role role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Users> findById(UUID userId);
}