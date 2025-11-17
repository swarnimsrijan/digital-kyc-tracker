package in.zeta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.constants.Messages;
import in.zeta.dto.requests.UpdateRoleRequest;
import in.zeta.dto.requests.UserRegistrationRequest;
import in.zeta.dto.response.UserResponse;
import in.zeta.enums.Role;
import in.zeta.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void testRegisterUser_Success() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .build();

        UserResponse response = UserResponse.builder()
                .userId(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .build();

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/tenants/{tenantId}/auth/register", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Auth.USER_REGISTERED_SUCCESSFULLY))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void testRegisterUser_InvalidRequest() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/tenants/{tenantId}/auth/register", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginUser_Success() throws Exception {
        String email = "test@example.com";
        String password = "password123";

        UserResponse response = UserResponse.builder()
                .username("testuser")
                .email(email)
                .role(Role.CUSTOMER)
                .build();

        when(userService.authenticateUser(email, password)).thenReturn(response);

        mockMvc.perform(post("/tenants/{tenantId}/auth/login", tenantId)
                        .param("email", email)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.Auth.LOGGING_IN_USER))
                .andExpect(jsonPath("$.data.email").value(email));

        verify(userService).authenticateUser(email, password);
    }

    @Test
    void testUpdateUserRole_Success() throws Exception {
        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .email("test@example.com")
                .role(Role.VERIFICATION_OFFICER)
                .build();

        UserResponse response = UserResponse.builder()
                .userId(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .role(Role.VERIFICATION_OFFICER)
                .build();

        when(userService.updateUserRole(eq("test@example.com"), eq(Role.VERIFICATION_OFFICER))).thenReturn(response);

        mockMvc.perform(put("/tenants/{tenantId}/auth/update/role", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Messages.User.USER_ROLE_UPDATED_SUCCESSFULLY))
                .andExpect(jsonPath("$.data.role").value(Role.VERIFICATION_OFFICER.name()));

        verify(userService).updateUserRole("test@example.com", Role.VERIFICATION_OFFICER);
    }
}