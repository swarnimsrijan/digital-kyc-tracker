package in.zeta.providers;

import in.zeta.constants.Messages;
import in.zeta.oms.sandbox.model.object.ObjectProvider;
import in.zeta.oms.sandbox.model.realm.Realm;
import in.zeta.dto.response.UserResponse;
import in.zeta.service.UserService;
import in.zeta.spectra.capture.SpectraLogger;
import olympus.common.JID;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class UserProvider implements ObjectProvider<UserResponse> {
    public static final String OBJECT_TYPE = "kycsw_user";
    private final UserService userService;
    private static final SpectraLogger logger = OlympusSpectra.getLogger(UserProvider.class);

    private static final String ERROR = "error";

    @Autowired
    public UserProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public CompletionStage<Optional<UserResponse>> getObject(JID jid, Realm realm, Long tenantID) {
        return CompletableFuture.supplyAsync(() -> {
            UserResponse user = null;
            UUID userId = null;

            try {
                userId = UUID.fromString(jid.getNodeId());

                logger.info("Entry: Fetching user object")
                        .attr(Messages.Keys.USER_ID, userId)
                        .log();

                user = userService.getUserById(userId);

                if (user == null) {

                    logger.error("User not found")
                            .attr(Messages.Keys.USER_ID, userId)
                            .log();

                    user = getDefaultUser();

                } else {

                    logger.info("Success: User object fetched")
                            .attr(Messages.Keys.USER_ID, userId)
                            .log();

                }

            } catch (Exception ex) {

                logger.error("Error fetching user object")
                        .attr(Messages.Keys.USER_ID, userId)
                        .attr(ERROR, ex.getMessage())
                        .log();

                user = getDefaultUser();
            }

            return Optional.of(user);
        });
    }

    private UserResponse getDefaultUser() {
        return new UserResponse();
    }
}