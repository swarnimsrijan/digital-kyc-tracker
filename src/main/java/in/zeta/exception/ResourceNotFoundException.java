package in.zeta.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }
}