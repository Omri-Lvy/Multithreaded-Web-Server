package enums;

public enum HttpStatusCode {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request", "400 Bad Request"),
    NOT_FOUND(404, "Not Found", "404 Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error", "500 Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented", "501 Not Implemented"),
    VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported", "505 HTTP Version Not Supported");


    private final int code;
    private final String message;
    private byte[] body;
    HttpStatusCode ( int code, String message ) {
        this.code = code;
        this.message = message;
    }

    HttpStatusCode ( int code, String message, String body) {
        this.code = code;
        this.message = message;
        this.body = body.getBytes();
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }

    public byte[] getBody() { return body; }

    public String toString() {
        return code + " " + message;
    }

}
