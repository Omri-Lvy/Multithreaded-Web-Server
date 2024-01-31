import server.ServerListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerTests {
    private static Thread serverThread;

    @BeforeClass
    public static void setUp() {
        // Start the server in a separate thread
        serverThread = new Thread(() -> WebServer.main(null));
        serverThread.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.out.println("Server configuration error: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        // Stop the server
        serverThread.interrupt();
    }

    @Test
    public void testServerConnection() throws IOException {
        String serverUrl = "http://localhost:8080/";

        String response = sendHttpRequest(serverUrl);
        assertEquals("HTTP/1.1 200 OK", response.substring(0, 15));
    }

    @Test
    public void testServerRequests() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        try {
            List<Future<String>> futures = new ArrayList<>();

            // Submit 20 tasks to the executor
            for (int i = 0; i < 20; i++) {
                Future<String> future = executorService.submit(() -> {
                    try {
                        return sendHttpRequest("http://localhost:8080/");
                    } catch (IOException ignored) {
                        return null;
                    }
                });
                futures.add(future);
            }

            // Wait for all tasks to complete
            for (Future<String> future : futures) {
                future.get(); // Block and wait for each task to complete

            }

            // Check if active thread count is not exceeding 10
            assertTrue("Active thread count should not exceed 10, exceeded " + ServerListener.threadPoolExecutor.getActiveCount(), ServerListener.threadPoolExecutor.getActiveCount() <= 10);
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(3, TimeUnit.SECONDS);
        }
    }


    private String sendHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        String protocolVersion = connection.getHeaderField(null);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            connection.disconnect();
        }

        return protocolVersion + System.lineSeparator() + response;
    }
}
