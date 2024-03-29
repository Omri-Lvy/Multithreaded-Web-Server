package server;

import exceptions.HttpParsingException;
import http.HttpRequest;
import http.HttpResponse;
import routing.Router;
import utils.HttpRequestParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientConnectionHandler extends Thread {
    private final Socket clientSocket;
    private final HttpRequestParser requestParser;
    private final Router router;
    private InputStream inputStream;
    private OutputStream outputStream;


    public ClientConnectionHandler (Socket clientSocket, Router router) {
        this.clientSocket = clientSocket;
        this.router = router;
        this.inputStream = null;
        this.outputStream = null;
        requestParser = new HttpRequestParser();
    }

    @Override
    public void run() {
        try {
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
            while (!Thread.interrupted() && clientSocket.isConnected() && !clientSocket.isClosed()) {
                if (inputStream.available() > 0) {
                    handleRequest();
                }
            }
        } catch (IOException e) {
            System.out.println("Error while handling request");
        } finally {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
            try {
                handleClose();
            } catch (IOException e) {
                System.out.println("Error while closing client connection");
            }
        }
    }

    private void handleRequest() throws IOException {
        byte[] requestBytes = new byte[inputStream.available()];
        requestBytes = inputStream.readNBytes(requestBytes.length);
        String clientAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        System.out.println("New request From " + clientAddress + ":");

        try {
            HttpRequest httpRequest = requestParser.parseHttpRequest(requestBytes, clientAddress);
            if (httpRequest != null) {
                StringBuilder request = new StringBuilder();
                request.append(httpRequest.getMethod()).append(" ").append(httpRequest.getRequestTarget()).append(" ").append(httpRequest.getVersion()).append("\r\n");
                for (String header : httpRequest.getHeaders().keySet()) {
                    request.append(header).append(": ").append(httpRequest.getHeaders().get(header)).append("\r\n");
                }
                System.out.println(request);
                router.router(httpRequest, outputStream);
            }
        } catch (HttpParsingException e) {
            String request = new String(requestBytes);
            System.out.println(request);
            System.out.println("Request Parsing FAILED!\r\n");
            HttpResponse.sendErrorResponseFailedParseRequest(clientAddress, outputStream, e.getErrorCode());
        }
    }

    private void handleClose() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }

        if (outputStream != null) {
            outputStream.close();
        }

        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
    }
}
