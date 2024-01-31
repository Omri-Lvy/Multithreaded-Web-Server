package exceptions;

import enums.HttpStatusCode;

public class HttpParsingException extends Exception {
    private final HttpStatusCode errorCode;

    public HttpParsingException(HttpStatusCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatusCode getErrorCode() {
        return errorCode;
    }
}
