package server;

import routing.Router;
import routing.Controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerListener extends Thread {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    private final Router router;

    public static ThreadPoolExecutor threadPoolExecutor;

    public ServerListener ( int port, int maxThreads, String rootDirectory, String defaultPage) throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(maxThreads);
        threadPoolExecutor = (ThreadPoolExecutor) threadPool;
        router = new Router(rootDirectory, defaultPage);
        Controller routes = new Controller();
        router.defineRoutes(routes);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(clientSocket, router);
                if (threadPoolExecutor.getActiveCount() < threadPoolExecutor.getMaximumPoolSize()) {
                    threadPool.submit(clientConnectionHandler);
                } else {
                    System.out.println("Client rejected: " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
                    OutputStream outputStream = clientSocket.getOutputStream();
                    outputStream.write("HTTP/1.1 503 Service Unavailable\r\n Content-Type: text/html\n".getBytes());
                    outputStream.flush();
                    outputStream.close();
                }
            }
        } catch (SocketException se) {
            System.out.println("is terminated: " + threadPool.isTerminated());
            System.out.println("SocketException: " + se.getMessage());
        } catch(IOException e) {
            if (!threadPool.isTerminated()) {
                System.out.println("Server error: " + e.getMessage());
                shutdown();
            }
        }
    }

    public void shutdown() {
        if (!threadPool.isShutdown()) {
            try {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(3, java.util.concurrent.TimeUnit.SECONDS)){
                    threadPool.shutdownNow();
                }
            } catch (Exception e) {
                System.out.println("Error while shutting down thread pool");
                System.exit(1);
            }
        }

        if (serverSocket.isBound() && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error while closing server socket");
                System.exit(1);
            }
        }
    }
}
