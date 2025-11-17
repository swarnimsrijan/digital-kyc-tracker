package in.zeta.exception;

import org.springframework.http.HttpStatus;

public class LimitExceededException extends BaseException {

    private static final String ERROR_CODE = "LIMIT_EXCEEDED";
    private static final HttpStatus STATUS = HttpStatus.TOO_MANY_REQUESTS;

    public LimitExceededException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public LimitExceededException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }
}