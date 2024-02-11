import config.Configuration;
import config.ConfigurationManager;
import server.ServerListener;

import java.io.IOException;
import java.util.Scanner;

public class WebServer {
    public static void main ( String[] args ) {
        try {
            ConfigurationManager configurationManager = ConfigurationManager.getInstance();
            Configuration configuration = configurationManager.getConfiguration();
            ServerListener serverListener = new ServerListener(
                    configuration.getPort(),
                    configuration.getMaxThreads(),
                    configuration.getRootDirectoryCanonicalPath(),
                    configuration.getDefaultPage()
            );
            System.out.println("Server started on port " + configuration.getPort());
            serverListener.start();
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter ':quit' or ':q' to stop the server");
            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase(":quit") || input.equalsIgnoreCase(":q")) {
                    System.out.println("Shutting down server...");
                    serverListener.shutdown();
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            System.out.println("Server configuration error: " + e.getMessage());
            System.exit(1);
        }

    }
}
