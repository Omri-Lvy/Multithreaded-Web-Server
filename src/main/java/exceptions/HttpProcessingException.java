package exceptions;

import enums.HttpStatusCode;

public class HttpProcessingException extends Exception {
    private final HttpStatusCode errorCode;

    public HttpProcessingException(HttpStatusCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatusCode getErrorCode() {
        return errorCode;
    }
}
