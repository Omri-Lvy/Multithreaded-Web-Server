package http;

import enums.HttpMethod;
import enums.HttpStatusCode;
import enums.HttpVersion;
import exceptions.HttpParsingException;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private HttpMethod method;
    private String requestTarget;
    private Map<String, String> headers;
    private final Map body;
    private HttpVersion version;
    private final String clientAddress;

    public HttpRequest(String clientAddress) {
        this.clientAddress = clientAddress;
        this.body = new HashMap();
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getRequestTarget() {
        return requestTarget;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    public void setVersion ( String version ) throws HttpParsingException {
        this.version = HttpVersion.getVersion(version);
    }

    public HttpVersion getVersion() {
        return version;
    }

    public Map getBody() {
        return body;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setMethod ( String methodName ) throws HttpParsingException {
        for (HttpMethod method: HttpMethod.values()) {
            if (methodName.equals(method.name())) {
                this.method = method;
                return;
            }
        }

        throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
    }

    public void setRequestTarget ( String requestTarget ) throws HttpParsingException {
        if (requestTarget == null || requestTarget.isEmpty()) {
            throw new HttpParsingException(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
        this.requestTarget = requestTarget;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(Map body) {
        this.body.putAll(body);
    }


}
