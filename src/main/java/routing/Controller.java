package routing;

import annotations.GET;
import annotations.HEAD;
import annotations.POST;
import annotations.TRACE;
import enums.ContentType;
import enums.HttpMethod;
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
    @GET("/params-info")
    @HEAD("/params-info")
    public void paramsInfo(HttpRequest request, String rootDirectory, OutputStream outputStream) {
        Map params = request.getBody();
        File htmlPage = new File(rootDirectory + "/params_info.html");
        StringBuilder paramsHTML = new StringBuilder();
        try {
            if (htmlPage.exists()) {
                byte[] htmlContent = Files.readAllBytes(htmlPage.toPath());
                String html = new String(htmlContent);
                for (Object key : params.keySet()) {
                    paramsHTML.append("<li class=\"param-item\">")
                            .append("<span class=\"param-name\">")
                            .append(key.toString().substring(0, 1).toUpperCase()).append(key.toString().substring(1)).append(": ")
                            .append("</span>")
                            .append("<span class=\"param-value\">")
                            .append(params.get(key).toString())
                            .append("</span>")
                            .append("</li>");
                }
                html = html.replace("{{params}}", paramsHTML.toString());
                if (request.getMethod() == HttpMethod.HEAD) {
                    HttpResponse.sendResponseHeader(request, outputStream, HttpStatusCode.OK, ContentType.HTML, html.getBytes().length);
                } else {
                    HttpResponse.sendResponse(request, outputStream, HttpStatusCode.OK, ContentType.HTML, html.getBytes());
                }
            } else {
                HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
            }
        } catch (IOException e) {
            HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GET("/params_info.html")
    @POST("/params_info.html")
    @HEAD("/params_info.html")
    public void getparamsInfo(HttpRequest request, String rootDirectory, OutputStream outputStream) {
        Map params = request.getBody();
        File htmlPage = new File(rootDirectory + "/params_info.html");
        StringBuilder paramsHTML = new StringBuilder();
        try {
            if (htmlPage.exists()) {
                byte[] htmlContent = Files.readAllBytes(htmlPage.toPath());
                String html = new String(htmlContent);
                for (Object key : params.keySet()) {
                    paramsHTML.append("<li class=\"param-item\">")
                            .append("<span class=\"param-name\">")
                            .append(key.toString().substring(0, 1).toUpperCase()).append(key.toString().substring(1)).append(": ")
                            .append("</span>")
                            .append("<span class=\"param-value\">")
                            .append(params.get(key).toString())
                            .append("</span>")
                            .append("</li>");
                }
                html = html.replace("{{params}}", paramsHTML.toString());
                if (request.getMethod() == HttpMethod.HEAD) {
                    HttpResponse.sendResponseHeader(request, outputStream, HttpStatusCode.OK, ContentType.HTML, html.getBytes().length);
                } else {
                    HttpResponse.sendResponse(request, outputStream, HttpStatusCode.OK, ContentType.HTML, html.getBytes());
                }
            } else {
                HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
            }
        } catch (IOException e) {
            HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GET("/*")
    @POST("/*")
    public void getFile(HttpRequest request, String rootDirectory, OutputStream outputStream, File requestedFile) {
        ContentType contentType = ContentType.fromString(requestedFile.toString().substring(requestedFile.toString().lastIndexOf(".") + 1));
        try {
            // Check if it both exist and is a file
            if (requestedFile.exists() && requestedFile.isFile()) {
                String reqResourceCanonicalPath = requestedFile.getCanonicalPath();
                if (reqResourceCanonicalPath.startsWith(rootDirectory))
                {
                    byte[] content = Files.readAllBytes(requestedFile.toPath());
                    HttpResponse.sendResponse(request, outputStream, HttpStatusCode.OK, contentType, content);
                }
                else {
                    System.out.println("Invalid file path");
                    HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
                }
            } else {
                HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
            }
        } catch (IOException e) {
            HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @HEAD("/*")
    public void getHeaders(HttpRequest request, String rootDirectory, OutputStream outputStream, File requestedFile) {
        ContentType contentType = ContentType.fromString(requestedFile.toString().substring(requestedFile.toString().lastIndexOf(".") + 1));
        try {
            // Check if it both exist and is a file
            if (requestedFile.exists() && requestedFile.isFile()) {
                String reqResourceCanonicalPath = requestedFile.getCanonicalPath();
                // Path Traversal Attack
                if (reqResourceCanonicalPath.startsWith(rootDirectory))
                {
                    HttpResponse.sendResponseHeader(request, outputStream, HttpStatusCode.OK, contentType, (int)requestedFile.length());
                }
                else {
                    System.out.println("PATH TRAVERSAL ATTACK");
                    HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
                }
            } else {
                HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
            }
        } catch (IOException e) {
            HttpResponse.sendErrorResponse(request, outputStream, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
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
        HttpResponse.sendResponse(request, outputStream, HttpStatusCode.OK, ContentType.DEFAULT, response);
    }
}
