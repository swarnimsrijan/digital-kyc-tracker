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
import in.zeta.producer.EventProducer;
import in.zeta.repository.UserRepository;
import in.zeta.dto.requests.UserRegistrationRequest;
import in.zeta.dto.response.UserResponse;
import in.zeta.service.AuditService;
import in.zeta.service.UserService;
import in.zeta.spectra.capture.SpectraLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static in.zeta.constants.Messages.Errors.FAILED_LOGIN_ATTEMPT;
import static in.zeta.constants.Messages.Keys.*;
import static in.zeta.constants.Messages.User.*;
import static in.zeta.mapper.AuditLogMapper.createAuditLogEvent;
import static in.zeta.mapper.UserMapper.toUserResponse;
import static in.zeta.mapper.UserMapper.toUser;

@Service
//@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SpectraLogger logger = OlympusSpectra.getLogger(UserServiceImpl.class);
    private final UserRepository usersRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository usersRepository,
                           @Lazy AuditService auditService,
                           PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse registerUser(@Valid UserRegistrationRequest registrationRequest) {

        logger.info(Messages.Auth.REGISTERING_NEW_USER)
                .attr(Messages.Keys.EMAIL, registrationRequest.getEmail())
                .attr(Messages.Keys.USERNAME, registrationRequest.getUsername())
                .log();

        if (usersRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new InvalidOperationException(Messages.Errors.EMAIL_ALREADY_EXISTS + " " + registrationRequest.getEmail());
        }

        if (usersRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            throw new InvalidOperationException(Messages.Errors.USERNAME_ALREADY_EXISTS + " " + registrationRequest.getUsername());
        }

        Role userRole = registrationRequest.getRole() != null ?
                registrationRequest.getRole() : Role.CUSTOMER;

        Users user = toUser(registrationRequest.getUsername(),
                registrationRequest.getEmail(),
                passwordEncoder.encode(registrationRequest.getPassword()),
                userRole);

        Users savedUser = usersRepository.save(user);

        logger.info(Messages.Auth.USER_REGISTERED_SUCCESSFULLY)
                .attr(USER_ID, savedUser.getId())
                .attr(Messages.Keys.EMAIL, savedUser.getEmail())
                .attr(Messages.Keys.USERNAME, savedUser.getUsername())
                .attr(Messages.Keys.ROLE, savedUser.getRole())
                .log();

        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.USER,
                savedUser.getId(),
                AuditAction.USER_CREATED,
                savedUser,
                null,
                String.format( USER_CREATED_WITH_ROLE+ ": %s, email: %s",
                        savedUser.getRole(), savedUser.getEmail())
        );

        auditService.publishAuditLogEvent(event);

        return toUserResponse(savedUser);
    }

    @Override
    public UserResponse authenticateUser(String email, String password) {

        logger.info(Messages.Auth.AUTHENTICATING_USER)
                .attr(Messages.Keys.EMAIL, email)
                .log();

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("users", "email", email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
//        if(!password.equals(user.getPassword())) {

            AuditLogCreatedEvent event = createAuditLogEvent(
                    EntityType.USER,
                    user.getId(),
                    AuditAction.ACCESS_DENIED,
                    user,
                    null,
                    String.format(FAILED_LOGIN_ATTEMPT + ": %s - Password mismatch", email)
            );

            auditService.publishAuditLogEvent(event);

            throw new InvalidOperationException(Messages.Errors.INVALID_PASSWORD + " " + email);
        }

        logger.info(Messages.Auth.USER_AUTHENTICATED_SUCCESSFULLY)
                .attr(USER_ID, user.getId())
                .attr(Messages.Keys.EMAIL, user.getEmail())
                .attr(Messages.Keys.USERNAME, user.getUsername())
                .attr(Messages.Keys.ROLE, user.getRole())
                .log();

        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.USER,
                user.getId(),
                AuditAction.USER_ROLE_CHANGED,
                user,
                null,
                String.format(Messages.Auth.LOGIN_SUCCESSFUL + ": %s", user.getUsername())
        );

        auditService.publishAuditLogEvent(event);

        return toUserResponse(user);
    }

    @Override
    public UserResponse updateUserRole(String email, Role role) {

        logger.info(Messages.User.UPDATING_USER_ROLE)
                .attr(Messages.Keys.EMAIL, email)
                .attr(Messages.Keys.ROLE, role)
                .log();

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("users", "email", email));

        Role oldRole = user.getRole();
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        Users updatedUser = usersRepository.save(user);

        logger.info(Messages.User.USER_ROLE_UPDATED_SUCCESSFULLY)
                .attr(EMAIL, updatedUser.getEmail())
                .attr(ROLE, oldRole)
                .attr(NEW_ROLE, updatedUser.getRole())
                .log();

        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.USER,
                updatedUser.getId(),
                AuditAction.USER_ROLE_CHANGED,
                updatedUser,
                null,
                String.format(UPDATING_USER_DETAILS + ": %s, to role: %s",
                        oldRole, updatedUser.getRole())
        );

        auditService.publishAuditLogEvent(event);

        return toUserResponse(updatedUser);
    }

    @Override
    public Users getByIdOrThrow(UUID userId, String notFoundMessage) {

        logger.info(FETCHING_USER_DETAILS)
                .attr(USER_ID, userId)
                .log();

        return usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));
    }

    @Override
    public List<Users> findByRole(Role role) {
        logger.info(FETCHING_USER_BY_ROLE)
                .attr(ROLE, role)
                .log();

        List<Users> users = usersRepository.findByRole(role);
        if (users == null || users.isEmpty()) {
            throw new DataNotFoundException("users", "role", role);
        }
        return users;
    }

    @Override
    public UserResponse getUserById(UUID userId) {

        logger.info(FETCHING_USER_DETAILS)
                .attr(USER_ID, userId)
                .log();

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("users", "id", userId));

        return toUserResponse(user);
    }
}