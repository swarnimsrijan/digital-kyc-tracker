package in.zeta.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends BaseException {

    private static final String ERROR_CODE = "INVALID_FILE";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvalidFileException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }
}