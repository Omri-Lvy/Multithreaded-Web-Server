package enums;

public enum HttpMethod {
    GET,
    POST,
    HEAD,
    TRACE;

    public static final int MAX_LENGTH;

    static {
        int tempMaxLength = -1;
        for (HttpMethod method : values()) {
            if (method.name().length() > tempMaxLength) {
                tempMaxLength = method.name().length();
            }
        }
        MAX_LENGTH = tempMaxLength;
    }
}
