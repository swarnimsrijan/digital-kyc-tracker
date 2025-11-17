package in.zeta.service.impl;

import in.zeta.constants.Messages;
import in.zeta.entity.Users;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import in.zeta.enums.Role;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.exception.DataNotFoundException;
import in.zeta.exception.InvalidOperationException;
import in.zeta.exception.ResourceNotFoundException;
import in.zeta.repository.UserRepository;
import in.zeta.dto.requests.UserRegistrationRequest;
import in.zeta.dto.response.UserResponse;
import in.zeta.service.AuditService;
import in.zeta.spectra.capture.SpectraLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository usersRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID testUserId;
    private Users testUser;
    private UserRegistrationRequest registrationRequest;
    private UserResponse expectedUserResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = Users.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .build();

        expectedUserResponse = UserResponse.builder()
                .userId(testUserId)
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    void registerUser_Success() {
        // Given
        when(usersRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(usersRepository.findByUsername(registrationRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.registerUser(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getRole(), result.getRole());
        verify(usersRepository).save(any(Users.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Given
        when(usersRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                () -> userService.registerUser(registrationRequest));

        assertTrue(exception.getMessage().contains(Messages.Errors.EMAIL_ALREADY_EXISTS));
        verify(usersRepository, never()).save(any(Users.class));
        verify(auditService, never()).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        // Given
        when(usersRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(usersRepository.findByUsername(registrationRequest.getUsername())).thenReturn(Optional.of(testUser));

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                () -> userService.registerUser(registrationRequest));

        assertTrue(exception.getMessage().contains(Messages.Errors.USERNAME_ALREADY_EXISTS));
        verify(usersRepository, never()).save(any(Users.class));
        verify(auditService, never()).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void registerUser_DefaultRole() {
        // Given
        UserRegistrationRequest requestWithoutRole = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(usersRepository.findByEmail(requestWithoutRole.getEmail())).thenReturn(Optional.empty());
        when(usersRepository.findByUsername(requestWithoutRole.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(requestWithoutRole.getPassword())).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.registerUser(requestWithoutRole);

        // Then
        assertNotNull(result);
        assertEquals(Role.CUSTOMER, result.getRole());
        verify(usersRepository).save(any(Users.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void authenticateUser_Success() {
        // Given
        String email = "test@example.com";
        String password = "password123";

        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // When
        UserResponse result = userService.authenticateUser(email, password);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void authenticateUser_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password123";

        when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.authenticateUser(email, password));

        assertTrue(exception.getMessage().contains("users") &&
                exception.getMessage().contains("email") &&
                exception.getMessage().contains(email));
        verify(auditService, never()).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void authenticateUser_InvalidPassword() {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";

        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                () -> userService.authenticateUser(email, password));

        assertTrue(exception.getMessage().contains(Messages.Errors.INVALID_PASSWORD));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void updateUserRole_Success() {
        // Given
        String email = "test@example.com";
        Role newRole = Role.ADMIN;
        Users updatedUser = Users.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(newRole)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(usersRepository.save(any(Users.class))).thenReturn(updatedUser);

        // When
        UserResponse result = userService.updateUserRole(email, newRole);

        // Then
        assertNotNull(result);
        assertEquals(newRole, result.getRole());
        verify(usersRepository).save(any(Users.class));
        verify(auditService).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void updateUserRole_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        Role newRole = Role.ADMIN;

        when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.updateUserRole(email, newRole));

        assertTrue(exception.getMessage().contains("users") &&
                exception.getMessage().contains("email") &&
                exception.getMessage().contains(email));
        verify(usersRepository, never()).save(any(Users.class));
        verify(auditService, never()).publishAuditLogEvent(any(AuditLogCreatedEvent.class));
    }

    @Test
    void getByIdOrThrow_Success() {
        // Given
        String notFoundMessage = "User not found with ID: " + testUserId;
        when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        Users result = userService.getByIdOrThrow(testUserId, notFoundMessage);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(usersRepository).findById(testUserId);
    }

    @Test
    void getByIdOrThrow_UserNotFound() {
        // Given
        String notFoundMessage = "User not found with ID: " + testUserId;
        when(usersRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getByIdOrThrow(testUserId, notFoundMessage));

        assertEquals(notFoundMessage, exception.getMessage());
        verify(usersRepository).findById(testUserId);
    }

    @Test
    void findByRole_Success() {
        // Given
        Role role = Role.CUSTOMER;
        List<Users> users = Arrays.asList(testUser);
        when(usersRepository.findByRole(role)).thenReturn(users);

        // When
        List<Users> result = userService.findByRole(role);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        verify(usersRepository).findByRole(role);
    }

    @Test
    void findByRole_EmptyList() {
        // Given
        Role role = Role.ADMIN;
        when(usersRepository.findByRole(role)).thenReturn(Arrays.asList());

        // When & Then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.findByRole(role));

        assertTrue(exception.getMessage().contains("users") &&
                exception.getMessage().contains("role") &&
                exception.getMessage().contains(role.toString()));
        verify(usersRepository).findByRole(role);
    }

    @Test
    void findByRole_NullList() {
        // Given
        Role role = Role.ADMIN;
        when(usersRepository.findByRole(role)).thenReturn(null);

        // When & Then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.findByRole(role));

        assertTrue(exception.getMessage().contains("users") &&
                exception.getMessage().contains("role") &&
                exception.getMessage().contains(role.toString()));
        verify(usersRepository).findByRole(role);
    }

    @Test
    void getUserById_Success() {
        // Given
        when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserById(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(usersRepository).findById(testUserId);
    }

    @Test
    void getUserById_UserNotFound() {
        // Given
        when(usersRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.getUserById(testUserId));

        assertTrue(exception.getMessage().contains("users") &&
                exception.getMessage().contains("id") &&
                exception.getMessage().contains(testUserId.toString()));
        verify(usersRepository).findById(testUserId);
    }
}