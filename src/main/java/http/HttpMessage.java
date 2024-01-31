package http;

import enums.HttpVersion;
import exceptions.HttpParsingException;

public class HttpMessage {
    private HttpVersion version;

    public void setVersion ( String version ) throws HttpParsingException {
        this.version = HttpVersion.getVersion(version);
    }

    public HttpVersion getVersion() {
        return version;
    }
}
