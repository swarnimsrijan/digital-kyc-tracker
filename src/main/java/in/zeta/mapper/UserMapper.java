package in.zeta.mapper;

import in.zeta.entity.Users;
import in.zeta.enums.Role;
import in.zeta.dto.response.UserResponse;

import java.time.LocalDateTime;

public class UserMapper {
    public static Users toUser(String username, String email, String hashedPassword, Role userRole) {
        return Users.builder()
                .username(username)
                .email(email)
                .password(hashedPassword)
                .role(userRole)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UserResponse toUserResponse(Users user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
    private UserMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
}
