package in.zeta.exception;

import org.springframework.http.HttpStatus;

public class InvalidOperationException extends BaseException {

    private static final String ERROR_CODE = "INVALID_OPERATION";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvalidOperationException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }
}