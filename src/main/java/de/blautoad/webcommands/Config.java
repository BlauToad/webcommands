package de.blautoad.webcommands;

import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config {
    // The config file name
    private static final String CONFIG_FILE = "webcommands.properties";
    // The config file object
    private static File configFile;
    // The properties object to store and load the config values
    private static Properties properties;

    // The port integer value
    private static int port;

    // A static initializer block to load the config file and values
    static {
        // Get the config directory from Fabric
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        // Create the config file object
        configFile = new File(configDir, CONFIG_FILE);
        // Create the properties object
        properties = new Properties();
        try {
            // If the config file does not exist, create it and set the default values
            if (!configFile.exists()) {
                configFile.createNewFile();
                setDefaultValues();
            }
            // Load the config values from the file
            loadValues();
        } catch (IOException e) {
            // Handle any IO exceptions
            e.printStackTrace();
        }
    }

    // A private method to set the default values for the config file
    private static void setDefaultValues() throws IOException {
        // Set the default port value to 80
        properties.setProperty("port", "80");
        // Save the properties to the config file
        properties.store(new FileWriter(configFile), "Port for the Webcommands Mod");
    }

    // A private method to load the values from the config file
    private static void loadValues() throws IOException {
        // Load the properties from the config file
        properties.load(new FileReader(configFile));
        // Parse the port value as an integer
        port = Integer.parseInt(properties.getProperty("port"));
    }

    // A public method to save the port value to the config file
    public static void savePort(int port) {
        // Set the port value in the properties object
        properties.setProperty("port", String.valueOf(port));
        try{
            // Save the properties to the config file
            properties.store(new FileWriter(configFile), "Port for the Webcommands Mod");
        }catch(IOException ignored){}
        // Update the port value in memory
        Config.port = port;
    }

    // A public method to get the port value from memory
    public static int getPort() {
        return port;
    }
}
