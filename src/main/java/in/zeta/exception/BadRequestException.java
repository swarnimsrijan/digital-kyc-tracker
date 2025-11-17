package in.zeta.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    private static final String ERROR_CODE = "BAD_REQUEST";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public BadRequestException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }
}