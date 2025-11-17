package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.providers.UserProvider;
import in.zeta.dto.requests.UpdateRoleRequest;
import in.zeta.dto.requests.UserRegistrationRequest;
import in.zeta.dto.response.ApiResponse;
import in.zeta.dto.response.UserResponse;
import in.zeta.service.UserService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAuthorizedSync;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static in.zeta.constants.Messages.*;
import static in.zeta.constants.Messages.Keys.NEW_ROLE;
import static in.zeta.constants.Messages.User.UPDATE_USER_ROLE;
import static in.zeta.constants.Messages.User.USER_ROLE_UPDATED_SUCCESSFULLY;

@RestController
@RequestMapping("/tenants/{tenantId}/auth")
@RequiredArgsConstructor
public class UserController {

    private final SpectraLogger logger = OlympusSpectra.getLogger(UserController.class);
    private final UserService userService;


    @PostMapping("/register")
//    @SandboxAuthorizedSync(action = "user.create", object = "$$tenants$$@" + UserProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody UserRegistrationRequest registrationDto) {

        logger.info(Auth.REGISTERING_NEW_USER)
                .attr(Messages.Keys.EMAIL, registrationDto.getEmail())
                .log();
        UserResponse userResponse = userService.registerUser(registrationDto);

        logger.info(Auth.USER_REGISTERED_SUCCESSFULLY)
                .attr(Keys.EMAIL, registrationDto.getEmail())
                .log();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Auth.USER_REGISTERED_SUCCESSFULLY, userResponse));
    }

    @PostMapping("/login")
    @SandboxAuthorizedSync(action = "user.create", object = "$$tenants$$@" + UserProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<UserResponse>> loginUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password) {

        logger.info(Auth.LOGGING_IN_USER)
                .attr(Keys.EMAIL, email)
                .log();

        UserResponse userResponse = userService.authenticateUser(email, password);

        logger.info(Auth.LOGIN_SUCCESSFUL)
                .attr(Keys.EMAIL, email)
                .log();

        return ResponseEntity.ok(ApiResponse.success(Auth.LOGGING_IN_USER, userResponse));

    }

    @PutMapping("/update/role")
    @SandboxAuthorizedSync(action = "user.update", object = "$$tenants$$@" + UserProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @RequestBody UpdateRoleRequest request) {

        logger.info(UPDATE_USER_ROLE)
                .attr(Keys.EMAIL, request.getEmail())
                .attr(NEW_ROLE, request.getRole())
                .log();

        UserResponse userResponse = userService.updateUserRole(request.getEmail(), request.getRole());

        logger.info(Messages.User.UPDATING_USER_ROLE)
                .attr(Messages.Keys.EMAIL, request.getEmail())
                .attr(NEW_ROLE, request.getRole())
                .log();

        return ResponseEntity.ok(ApiResponse.success(USER_ROLE_UPDATED_SUCCESSFULLY, userResponse));

    }
}