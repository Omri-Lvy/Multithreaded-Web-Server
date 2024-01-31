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
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;

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
            if (method.isAnnotationPresent(GET.class)) {
                String route = method.getAnnotation(GET.class).value();
                routes.put(HttpMethod.GET + route, handler);
            } else if (method.isAnnotationPresent(POST.class)) {
                String route = method.getAnnotation(POST.class).value();
                routes.put(HttpMethod.POST + route, handler);
            } else if (method.isAnnotationPresent(HEAD.class)) {
                String route = method.getAnnotation(HEAD.class).value();
                routes.put(HttpMethod.HEAD + route, handler);
            } else if (method.isAnnotationPresent(TRACE.class)) {
                String route = method.getAnnotation(TRACE.class).value();
                routes.put(HttpMethod.TRACE + route, handler);
            }
        }
    }

    public void router (HttpRequest request, OutputStream outputStream) {
        String path = request.getRequestTarget();
        Object handler = routes.get(request.getMethod().toString()+path);
        if (handler == null) {
            handler = routes.get(request.getMethod().toString()+"/*");
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
        Object[] args = null;
        Method methodToInvoke = null;
        Method[] methods = handler.getClass().getDeclaredMethods();
        try {
            for (Method method : methods) {
                String routeAnnotation = getRouteAnnotation(method);
                HttpMethod methodAnnotation = getMethodAnnotation(method);
                if ((request.getRequestTarget().equals(routeAnnotation)) && (request.getMethod().equals(methodAnnotation))) {
                    args = switch (method.getName()) {
                        case "index" -> new Object[]{request, rootDirectory, defaultPage, outputStream };
                        case "paramsInfo" -> new Object[]{request, rootDirectory, outputStream };
                        default -> args;
                    };
                    methodToInvoke = method;
                    break;
                }
            }
            if (methodToInvoke == null) {
                if (request.getMethod().equals(HttpMethod.GET)) {
                    args = new Object[]{request, outputStream, requestedResource};
                    methodToInvoke = handler.getClass().getMethod("getFile", HttpRequest.class, OutputStream.class, File.class);
                } else if (request.getMethod().equals(HttpMethod.HEAD)) {
                    args = new Object[]{request, outputStream, requestedResource};
                    methodToInvoke = handler.getClass().getMethod("getHeaders", HttpRequest.class, OutputStream.class, File.class);
                } else if (request.getMethod().equals(HttpMethod.TRACE)) {
                    args = new Object[]{outputStream, request};
                    methodToInvoke = handler.getClass().getMethod("trace", OutputStream.class, HttpRequest.class);
                }
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

    private static String getRouteAnnotation (Method method) {
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

    private static HttpMethod getMethodAnnotation (Method method) {
        HttpMethod methodAnnotation = null;
        if (method.isAnnotationPresent(GET.class)) {
            methodAnnotation = HttpMethod.GET;
        } else if (method.isAnnotationPresent(POST.class)) {
            methodAnnotation = HttpMethod.POST;
        } else if (method.isAnnotationPresent(HEAD.class)) {
            methodAnnotation = HttpMethod.HEAD;
        } else if (method.isAnnotationPresent(TRACE.class)) {
            methodAnnotation = HttpMethod.TRACE;
        }
        return methodAnnotation;
    }

}
