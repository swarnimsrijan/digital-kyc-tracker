package in.zeta.exception;

import org.springframework.http.HttpStatus;

public class AuditLogException extends BaseException {

    private static final String ERROR_CODE = "AUDIT_LOG_FAILED";
    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public AuditLogException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public AuditLogException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }

    public AuditLogException(String message, String errorCode) {
        super(message, STATUS, errorCode);
    }
}