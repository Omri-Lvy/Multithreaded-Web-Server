package config;

public class Configuration {
    private int port;
    private int maxThreads;
    private String rootDirectory;
    private String defaultPage;

    public int getPort () {
        return port;
    }

    public void setPort ( int port ) {
        this.port = port;
    }
    public int getMaxThreads () {
        return maxThreads;
    }

    public void setMaxThreads ( int maxThreads ) {
        this.maxThreads = maxThreads;
    }

    public String getRootDirectory () {
        return rootDirectory;
    }

    public void setRootDirectory ( String rootDirectory ) {
        this.rootDirectory = rootDirectory;
    }

    public String getDefaultPage () {
        return defaultPage;
    }

    public void setDefaultPage ( String defaultPage ) {
        this.defaultPage = defaultPage;
    }


}
