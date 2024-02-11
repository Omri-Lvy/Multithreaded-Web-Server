package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static ConfigurationManager configurationManager;
    private final Configuration configuration;

    private ConfigurationManager() throws IOException {
        configuration = new Configuration();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.ini")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            configuration.setPort(Integer.parseInt(properties.getProperty("port")));
            configuration.setMaxThreads(Integer.parseInt(properties.getProperty("maxThreads")));
            configuration.setRootDirectory(properties.getProperty("root"));
            configuration.setDefaultPage(properties.getProperty("defaultPage"));

            try {
                File rootDir = new File(configuration.getRootDirectory());
                configuration.setRootDirectoryCanonicalPath(rootDir.getCanonicalPath() + File.separator);
            } catch (java.io.IOException e) {
                System.out.println("Could not resolve root directory: " + e.getMessage());
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Config file not found");
            System.exit(1);
        }
    }

    public static ConfigurationManager getInstance() throws IOException {
        if (configurationManager == null) {
            configurationManager = new ConfigurationManager();
        }
        return configurationManager;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
