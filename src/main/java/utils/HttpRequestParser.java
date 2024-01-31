package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import enums.HttpMethod;
import enums.HttpStatusCode;
import exceptions.HttpParsingException;
import http.HttpRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpRequestParser {
    private static final int SP = 0x20; // 32
    private static final int CR = 0x0D; // 13
    private static final int LF = 0x0A; // 10


    public void parseHttpRequest (HttpRequest request, byte[] requestBytes) throws HttpParsingException {
        ByteArrayInputStream stream = new ByteArrayInputStream(requestBytes);
        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.US_ASCII);

        try {
            parseRequestLine(reader, request);
            parseHeaders(reader, request);
            parseBody(reader, request);
        } catch (IOException e) {
            throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
        }
    }

    public HttpRequest parseHttpRequest (byte[] requestBytes, String clientAddress) throws HttpParsingException {
        HttpRequest request = new HttpRequest(clientAddress);
        parseHttpRequest(request, requestBytes);

        return request;
    }

    private void parseRequestLine (InputStreamReader reader, HttpRequest request) throws IOException, HttpParsingException {
        StringBuilder requestLineDataBuffer = new StringBuilder();
        boolean isMethodParsed = false;
        boolean isRequestedTargetParsed = false;
        int byteRead;

        while ((byteRead = reader.read()) != -1) {
            if (byteRead == CR) {
                byteRead = reader.read();
                if (byteRead == LF) {
                    if (!isMethodParsed || !isRequestedTargetParsed) {
                        throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                    }
                    try {
                        request.setVersion(requestLineDataBuffer.toString());
                    } catch (HttpParsingException e) {
                        throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                    }
                    return;
                } else {
                    throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                }
            }
            if (byteRead == SP) {
                if (!isMethodParsed) {
                    request.setMethod(requestLineDataBuffer.toString());
                    isMethodParsed = true;
                } else if (!isRequestedTargetParsed) {
                    request.setRequestTarget(requestLineDataBuffer.toString());
                    isRequestedTargetParsed = true;
                } else {
                    throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                }
                requestLineDataBuffer.delete(0, requestLineDataBuffer.length());
            } else {
                requestLineDataBuffer.append((char) byteRead);
                if (!isMethodParsed) {
                    if (requestLineDataBuffer.length() > HttpMethod.MAX_LENGTH) {
                        throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                    }
                }
            }
        }
    }

    private void parseHeaders ( InputStreamReader reader, HttpRequest request ) throws IOException, HttpParsingException {
        StringBuilder headerLineDataBuffer = new StringBuilder();
        Map<String, String> headers = new HashMap<>();
        int crlfCount = 0;
        int byteRead;

        while ((byteRead = reader.read()) != -1) {
            if (byteRead == CR) {
                byteRead = reader.read();
                if (byteRead == LF) {
                    crlfCount++;
                    if (crlfCount == 2) {
                        request.setHeaders(headers);
                        return;
                    }
                    String headerLine = headerLineDataBuffer.toString();
                    int separatorIndex = headerLine.indexOf(":");
                    if (separatorIndex == -1) {
                        throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                    }
                    String name = headerLine.substring(0, separatorIndex).trim();
                    String value = headerLine.substring(separatorIndex + 1).trim();
                    headers.put(name, value);
                    // Reset the buffer for the next header line
                    headerLineDataBuffer.setLength(0);
                } else {
                    throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                }
            } else {
                crlfCount = 0;
                headerLineDataBuffer.append((char) byteRead);
            }
        }
    }

    private void parseBody ( InputStreamReader reader, HttpRequest request ) throws IOException, HttpParsingException {
        String contentLengthHeader = request.getHeader("Content-Length");
        String contentTypeHeader = request.getHeader("Content-Type");
        if (contentLengthHeader != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthHeader);
                char[] bodyData = new char[contentLength];
                int bytesRead = reader.read(bodyData, 0, contentLength);
                if (bytesRead == contentLength) {
                    String requestBody = new String(bodyData);
                    if (contentTypeHeader != null) {
                        if (contentTypeHeader.equals("application/json")) {
                            request.setBody(parseJsonBody(requestBody));
                        } else if (contentTypeHeader.equals("application/x-www-form-urlencoded")) {
                            request.setBody(parseUrlEncodedBody(requestBody));
                        }
                    } else {
                        throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                    }
                } else {
                    // Handle the case where the body size doesn't match the Content-Length header
                    throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
                }
            } catch (NumberFormatException e) {
                // Handle the case where Content-Length is not a valid integer
                throw new HttpParsingException(HttpStatusCode.BAD_REQUEST);
            }
        }
    }

    public static Map<Object, Object> parseUrlEncodedBody(String body) {
        Map<Object, Object> formData = new HashMap<>();
        String[] pairs = body.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                formData.put(key, value);
            } else {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                formData.put(key, "");
            }
        }

        return formData;
    }

    public static Map parseJsonBody( String body) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(body, Map.class);
    }
}
