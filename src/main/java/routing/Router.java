package routing;

import annotations.GET;
import annotations.HEAD;
import annotations.POST;
import annotations.TRACE;
import enums.HttpMethod;
import enums.HttpStatusCode;
import exceptions.HttpProcessingException;
import http.HttpRequest;

import java.io.File;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static http.HttpResponse.sendErrorResponse;

public class Router {
    private final Map<String, Object> routes = new HashMap<>();
    private final String rootDirectory;
    private final String defaultPage;

    public Router (String rootDirectory, String defaultPage) {
        this.rootDirectory = rootDirectory;
        this.defaultPage = defaultPage;
    }

    public void defineRoutes (Object handler) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(GET.class)) {
                    String route = ((GET) annotation).value();
                    routes.put(HttpMethod.GET + route, handler);
                } else if (annotation.annotationType().equals(POST.class)) {
                    String route = ((POST) annotation).value();
                    routes.put(HttpMethod.POST + route, handler);
                } else if (annotation.annotationType().equals(HEAD.class)) {
                    String route = ((HEAD) annotation).value();
                    routes.put(HttpMethod.HEAD + route, handler);
                } else if (annotation.annotationType().equals(TRACE.class)) {
                    String route = ((TRACE) annotation).value();
                    routes.put(HttpMethod.TRACE + route, handler);
                }
            }
        }
    }

    public boolean isMethodSupported (HttpMethod method) {
        return method.equals(HttpMethod.GET) || method.equals(HttpMethod.POST) || method.equals(HttpMethod.HEAD) || method.equals(HttpMethod.TRACE);
    }

    public void router (HttpRequest request, OutputStream outputStream) {
        if (!isMethodSupported(request.getMethod())) {
            sendErrorResponse(request, outputStream, HttpStatusCode.NOT_IMPLEMENTED);
            return;
        }

        String path = request.getRequestTarget();
        String route = request.getMethod().toString() + path;
        Object handler = routes.get(route);
        if (handler == null) {
            handler = routes.get(request.getMethod().toString()+"/*");
        }

        if (handler == null) {
            sendErrorResponse(request, outputStream, HttpStatusCode.INTERNAL_SERVER_ERROR);
            return;
        }

        File requestedResource = getRequestedResourceFile(path);
        try {
            invokeHandlerMethod(handler, request, outputStream, requestedResource);
        } catch (HttpProcessingException e) {
            sendErrorResponse(request, outputStream, HttpStatusCode.NOT_FOUND);
        }
    }

    private File getRequestedResourceFile( String requestedResource) {
        if (requestedResource.equals("/")) {
            requestedResource = defaultPage;
        }
        String resourcePath = rootDirectory + requestedResource;
        return new File(resourcePath);
    }

    private void invokeHandlerMethod (Object handler, HttpRequest request, OutputStream outputStream, File requestedResource) throws HttpProcessingException {
        Method[] methods = handler.getClass().getDeclaredMethods();
        try {
            for (Method method : methods) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    String routeAnnotation = getRouteAnnotation(method);
                    HttpMethod methodAnnotation = getMethodAnnotation(annotation);
                    if (routeMatches(request, routeAnnotation) && methodMatches(request.getMethod(), methodAnnotation)) {
                        Object[] args = getArguments(method, request, outputStream, requestedResource);
                        method.invoke(handler, args);
                        return;
                    }
                }
            }
            Method methodToInvoke = null;
            Object[] args = null;
            if (request.getMethod().equals(HttpMethod.GET) || request.getMethod().equals(HttpMethod.POST)) {
                args = new Object[] {request, rootDirectory, outputStream, requestedResource};
                methodToInvoke = handler.getClass().getMethod("getFile", HttpRequest.class, String.class, OutputStream.class, File.class);
            } else if (request.getMethod().equals(HttpMethod.HEAD)) {
                args = new Object[] {request, rootDirectory, outputStream, requestedResource};
                methodToInvoke = handler.getClass().getMethod("getHeaders", HttpRequest.class, String.class, OutputStream.class, File.class);
            } else if (request.getMethod().equals(HttpMethod.TRACE)) {
                args = new Object[] {outputStream, request};
                methodToInvoke = handler.getClass().getMethod("trace", OutputStream.class, HttpRequest.class);
            }
            if (methodToInvoke != null) {
                methodToInvoke.invoke(handler, args);
                return;
            }
            throw new HttpProcessingException(HttpStatusCode.NOT_FOUND);
        } catch (Exception e) {
            throw new HttpProcessingException(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean routeMatches(HttpRequest request, String routeAnnotation) {
        return routeAnnotation.equals(request.getRequestTarget());
    }

    private boolean methodMatches(HttpMethod requestMethod, HttpMethod methodAnnotation) {
        return requestMethod.equals(methodAnnotation);
    }

    private Object[] getArguments(Method method, HttpRequest request, OutputStream outputStream, File requestedResource) {
        return switch (method.getName()) {
            case "index" -> new Object[]{ request, rootDirectory, defaultPage, outputStream };
            case "paramsInfo", "getparamsInfo" -> new Object[]{ request, rootDirectory, outputStream };
            case "getFile", "getHeaders" -> new Object[]{ request, rootDirectory, outputStream, requestedResource };
            case "trace" -> new Object[]{ outputStream, request };
            default -> null;
        };
    }

    private static String getRouteAnnotation(Method method) {
        String routeAnnotation = null;
        if (method.isAnnotationPresent(GET.class)) {
            routeAnnotation = method.getAnnotation(GET.class).value();
        } else if (method.isAnnotationPresent(POST.class)) {
            routeAnnotation = method.getAnnotation(POST.class).value();
        } else if (method.isAnnotationPresent(HEAD.class)) {
            routeAnnotation = method.getAnnotation(HEAD.class).value();
        } else if (method.isAnnotationPresent(TRACE.class)) {
            routeAnnotation = method.getAnnotation(TRACE.class).value();
        }
        return routeAnnotation;
    }

    private static HttpMethod getMethodAnnotation (Annotation annotation) {
        HttpMethod methodAnnotation = null;
        String annotationType = annotation.annotationType().getCanonicalName().split("\\.")[1];
        methodAnnotation = switch (annotationType) {
            case "GET" -> HttpMethod.GET;
            case "POST" -> HttpMethod.POST;
            case "HEAD" -> HttpMethod.HEAD;
            case "TRACE" -> HttpMethod.TRACE;
            default -> methodAnnotation;
        };
        return methodAnnotation;
    }
}
