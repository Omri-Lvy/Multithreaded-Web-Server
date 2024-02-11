package enums;

public enum ContentType {
    HTML("text/html"),
    TEXT("text/plain"),
    PNG("image/png"),
    JPG("image/jpg"),
    BMP("image/bmp"),
    GIF("image/gif"),
    ICO("image/x-icon"),
    MESSAGE_HTTP("message/http"),
    DEFAULT("application/octet-stream");

    private final String contentType;

    ContentType ( String contentType) {
        this.contentType = contentType;
    }

    public String toString() {
        return contentType;
    }

    public static ContentType fromString( String contentType) {
        String requestedType = contentType.substring(contentType.indexOf(".") + 1);
        return switch (requestedType) {
            case "html" -> HTML;
            case "png" -> PNG;
            case "jpg" -> JPG;
            case "bmp" -> BMP;
            case "gif" -> GIF;
            case "ico" -> ICO;
            default -> DEFAULT;
        };
    }
}
