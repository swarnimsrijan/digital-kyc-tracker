package in.zeta.exception;

import org.springframework.http.HttpStatus;

public class InvalidStatusTransitionException extends BaseException {

    private static final String ERROR_CODE = "INVALID_STATUS_TRANSITION";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvalidStatusTransitionException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }
}