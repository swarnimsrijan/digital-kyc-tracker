package in.zeta.mapper;

import in.zeta.entity.Users;
import in.zeta.enums.Role;
import in.zeta.dto.response.UserResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUser_success() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "hashedpassword";
        Role role = Role.CUSTOMER;

        Users user = UserMapper.toUser(username, email, password, role);

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void toUserResponse_success() {
        Users user = Users.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .build();

        UserResponse response = UserMapper.toUserResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole(), response.getRole());
    }
}