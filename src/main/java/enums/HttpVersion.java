package enums;


import exceptions.HttpParsingException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum HttpVersion {
    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1");

    private final String LITERAL;
    private static final Pattern httpVersionRegexPattern = Pattern.compile("^HTTP/(?<major>\\d+).(?<minor>\\d+)");

    HttpVersion ( String literal) {
        this.LITERAL = literal;
    }

    public static HttpVersion getVersion(String literalVersion) throws HttpParsingException {
        Matcher matcher = httpVersionRegexPattern.matcher(literalVersion);
        if (!matcher.find() || matcher.groupCount() != 2) {
            throw new HttpParsingException(HttpStatusCode.VERSION_NOT_SUPPORTED);
        }
        HttpVersion version = null;
        for (HttpVersion httpVersion : HttpVersion.values()) {
            if (httpVersion.LITERAL.equals(literalVersion)) {
                version = httpVersion;
                break;
            }
        }

        return version;
    }

    public String toString() {
        return LITERAL;
    }

}
