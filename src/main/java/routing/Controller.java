package routing;

import annotations.GET;
import annotations.HEAD;
import annotations.POST;
import annotations.TRACE;
import enums.ContentType;
import enums.HttpStatusCode;
import http.HttpRequest;
import http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;


public class Controller {

    @GET("/")
    public void index(HttpRequest request, String rootDirectory, String defaultPage, OutputStream outputStream) {
        File indexPage = new File(rootDirectory + defaultPage);
        try {
            if (indexPage.exists()) {
                byte[] indexContent = Files.readAllBytes(indexPage.toPath());
                HttpResponse.sendResponse(request, outputStream, HttpStatusCode.OK, ContentType.HTML ,indexContent);
            } else {
                HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
            }
        } catch (IOException e) {
            HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @POST("/params-info")
    public void paramsInfo(HttpRequest request, String rootDirectory, OutputStream outputStream) {
        Map params = request.getBody();
        File htmlPage = new File(rootDirectory + "/params_info.html");
        try {
            if (htmlPage.exists()) {
                byte[] htmlContent = Files.readAllBytes(htmlPage.toPath());
                String html = new String(htmlContent);
                for (Object key : params.keySet()) {
                    if (key.equals("agree-terms-and-conditions")) {
                        html = html.replace("{{" + key + "}}", "True");
                    } else {
                        html = html.replace("{{" + key + "}}", params.get(key).toString());
                    }
                }
                if (!params.containsKey("agree-terms-and-conditions")) {
                    html = html.replace("{{agree-terms-and-conditions}}", "False");
                }
                HttpResponse.sendResponse(request, outputStream, HttpStatusCode.OK, ContentType.HTML, html.getBytes());
            } else {
                HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
            }
        } catch (IOException e) {
            HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GET("/*")
    public void getFile(HttpRequest request, OutputStream outputStream, File requestedFile) {
        ContentType contentType = ContentType.fromString(requestedFile.toString().substring(requestedFile.toString().lastIndexOf(".") + 1));
        try {
            if (requestedFile.exists()) {
                byte[] content = Files.readAllBytes(requestedFile.toPath());
                HttpResponse.sendResponse(request, outputStream, HttpStatusCode.OK, contentType, content);
            } else {
                HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
            }
        } catch (IOException e) {
            HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @HEAD("/*")
    public void head(HttpRequest request, OutputStream outputStream, File requestedFile) {
        ContentType contentType = ContentType.fromString(requestedFile.toString().substring(requestedFile.toString().lastIndexOf(".") + 1));
        int contentLength = (int) requestedFile.length();
        HttpResponse.sendResponseHeader(request, outputStream, HttpStatusCode.OK, contentType, contentLength);
    }

    @TRACE("/*")
    public void trace(OutputStream outputStream, HttpRequest request) {
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(request.getMethod()).append(" ").append(request.getRequestTarget()).append(" ").append(request.getVersion()).append("\r\n");
        Map<String, String> headers = request.getHeaders();
        for (String key : headers.keySet()) {
            requestBuilder.append(key).append(": ").append(headers.get(key)).append("\r\n");
        }
        requestBuilder.append("\r\n");
        Map requestBody = request.getBody();
        if (requestBody != null) {
            for (Object key : requestBody.keySet()) {
                requestBuilder.append(key).append(": ").append(requestBody.get(key)).append("\r\n");
            }
        }
        byte[] response = requestBuilder.toString().getBytes();
        HttpResponse.sendResponse(request, outputStream, HttpStatusCode.OK, ContentType.TEXT, response);
    }
}
