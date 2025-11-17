package in.zeta.exception;

import org.springframework.http.HttpStatus;

public class JsonParsingException extends BaseException {

    private static final String ERROR_CODE = "JSON_PARSING_ERROR";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public JsonParsingException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public JsonParsingException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }
}