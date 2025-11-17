package in.zeta.service;

import in.zeta.entity.Users;
import in.zeta.enums.Role;
import in.zeta.dto.requests.UserRegistrationRequest;
import in.zeta.dto.response.UserResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse registerUser(@Valid UserRegistrationRequest registrationRequest);
    UserResponse authenticateUser(String email, String password);
    UserResponse updateUserRole(String email, Role role);
    Users getByIdOrThrow(UUID userId, String notFoundMessage);
    List<Users> findByRole(Role role);
    UserResponse getUserById(UUID userId);
}
