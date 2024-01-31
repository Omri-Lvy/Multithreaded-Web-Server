package http;

import enums.ContentType;
import enums.HttpStatusCode;
import enums.HttpVersion;

import java.io.IOException;
import java.io.OutputStream;

public class HttpResponse {
    public static void sendResponse(HttpRequest request, OutputStream outputStream, HttpStatusCode statusCode, ContentType contentType, byte[] content) {
        String CRLF = "\r\n";
        String header = HttpVersion.HTTP_1_1 + " " + statusCode + CRLF +
                "Content-Type: " + contentType + CRLF +
                "Content-Length: " + content.length + CRLF +
                CRLF;
        if (request.getHeader("chunked").equals("yes")) {
            header = HttpVersion.HTTP_1_1 + " " + statusCode + CRLF +
                    "Content-Type: " + contentType + CRLF +
                    "Transfer-Encoding: chunked" + CRLF +
                    CRLF;
            printResponseHeaders(request, header);
            sendChunkedResponse(header, outputStream, content);
        }
        else {
            printResponseHeaders(request, header);
            sendResponse(header, content, outputStream);
        }
    }

    public static void sendResponseHeader(HttpRequest request, OutputStream outputStream, HttpStatusCode statusCode, ContentType contentType,int contentLength) {
        String CRLF = "\r\n";
        String header = HttpVersion.HTTP_1_1 + " " + statusCode + CRLF +
                "Content-Type: " + contentType + CRLF +
                "Content-Length: " + contentLength + CRLF +
                CRLF;
        printResponseHeaders(request, header);
        sendResponse(header, new byte[]{}, outputStream);
    }

    public static void sendErrorResponse(HttpRequest request, OutputStream outputStream, HttpStatusCode statusCode) {
        String CRLF = "\r\n";
        String header = HttpVersion.HTTP_1_1 + " " + statusCode + CRLF +
                "Content-Type: " + ContentType.DEFAULT + CRLF +
                "Content-Length: " + 0 + CRLF +
                CRLF;
        printResponseHeaders(request, header);
        sendResponse(header, new byte[]{}, outputStream);
    }

    private static void sendChunkedResponse(String header, OutputStream outputStream, byte[] content) {
        String CRLF = "\r\n";
        try {
            outputStream.write(header.getBytes());
            outputStream.flush();
            for (int i = 0; i < content.length; i += 1024) {
                int chunkSize = Math.min(1024, content.length - i);
                String chunkSizeHex = Integer.toHexString(chunkSize) + CRLF;
                outputStream.write(chunkSizeHex.getBytes());
                outputStream.write(content, i, chunkSize);
                outputStream.write(CRLF.getBytes());
                outputStream.flush();
            }
            outputStream.write("0\r\n\r\n".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Error while sending response");
        }
    }

    private static void sendResponse(String header, byte[] content, OutputStream outputStream) {
        try {
            outputStream.write(header.getBytes());
            if (content.length > 0) {
                outputStream.write(content);
            }
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Error while sending response");
        }
    }

    private static void printResponseHeaders(HttpRequest request, String header) {
        System.out.print("Response to: " + request.getClientAddress() + " " + request.getMethod() + " " + request.getRequestTarget() + "\r\n" + header);
    }
}
