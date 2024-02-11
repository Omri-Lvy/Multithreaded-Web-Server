package exceptions;

import enums.HttpStatusCode;

public class HttpProcessingException extends Exception {

    public HttpProcessingException(HttpStatusCode errorCode) {
        super(errorCode.getMessage());
    }
}
