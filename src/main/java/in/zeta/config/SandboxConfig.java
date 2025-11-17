package in.zeta.config;

import in.zeta.providers.*;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAccessControlProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SandboxConfig {

    @Bean
    @Primary
    public SandboxAccessControlProvider getSandboxAccessControlProvider(
            UserProvider userProvider,
            DocumentsProvider documentsProvider,
            VerificationRequestsProvider verificationRequestsProvider,
            NotificationProvider notificationProvider,
            CommentProvider commentProvider,
            AuditLogsProvider auditLogsProvider,
            StatusHistoryProvider statusHistoryProvider,
            SandboxAccessControlProvider sacp
    ) {
        sacp.registerObjectProvider(UserProvider.OBJECT_TYPE, userProvider);
        sacp.registerObjectProvider(DocumentsProvider.OBJECT_TYPE, documentsProvider);
        sacp.registerObjectProvider(VerificationRequestsProvider.OBJECT_TYPE, verificationRequestsProvider);
        sacp.registerObjectProvider(NotificationProvider.OBJECT_TYPE, notificationProvider);
        sacp.registerObjectProvider(CommentProvider.OBJECT_TYPE, commentProvider);
        sacp.registerObjectProvider(AuditLogsProvider.OBJECT_TYPE, auditLogsProvider);
        sacp.registerObjectProvider(StatusHistoryProvider.OBJECT_TYPE, statusHistoryProvider);
        return sacp;
    }
}