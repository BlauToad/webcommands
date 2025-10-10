package de.blautoad.webcommands;

import com.google.gson.Gson;
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
    // The fallbackPort integer value
    private static int[] fallbackPorts;
    // The port integer value
    private static boolean temp_port = false;

    private static final Gson gson = new Gson();

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
        int[] default_fbp = new int[0];
        String fbp = Config.gson.toJson(default_fbp);
        properties.setProperty("fallbackPorts", fbp);
        // Save the properties to the config file
        properties.store(new FileWriter(configFile), "Ports for the Webcommands Mod\nfallbackPorts are an int array, so in this format: [8080] and for multiple [8080,8040] and it will go from left to right to find the next free port");
    }

    // A private method to load the values from the config file
    private static void loadValues() throws IOException {
        // Load the properties from the config file
        properties.load(new FileReader(configFile));
        // Parse the port value as an integer
        Config.port = Integer.parseInt(properties.getProperty("port"));
        Config.fallbackPorts = Config.gson.fromJson(properties.getProperty("fallbackPorts"), int[].class);

    }

    // A public method to save the port value to the config file
    public static void savePort(int port) {
        // Set the port value in the properties object
        properties.setProperty("port", String.valueOf(port));
        String fbp = Config.gson.toJson(Config.fallbackPorts);
        properties.setProperty("fallbackPorts", fbp);
        try{
            // Save the properties to the config file
            properties.store(new FileWriter(configFile), "Port for the Webcommands Mod");
        }catch(IOException ignored){}
        // Update the port value in memory
        Config.port = port;
        Config.temp_port = false;
    }
    // A public method to get the port value from memory
    public static int getPort() {
        return Config.port;
    }
    // A public method to save the port value to the config file
    public static void saveFallbackPort(int[] fallbackPorts) {
        // Set the port value in the properties object
        properties.setProperty("port", String.valueOf(Config.port));
        String fbp = Config.gson.toJson(fallbackPorts);
        properties.setProperty("fallbackPorts", fbp);
        try{
            // Save the properties to the config file
            properties.store(new FileWriter(configFile), "Fallbackports for the Webcommands Mod");
        }catch(IOException ignored){}
        // Update the port value in memory
        Config.fallbackPorts = fallbackPorts;
    }

    // A public method to get the port value from memory
    public static int[] getFallbackPorts() {
        return Config.fallbackPorts;
    }

    public static void tempChangePort(int port){
        Config.port = port;
        Config.temp_port = true;
    }

    public static boolean isTempPort(){
        return Config.temp_port;
    }
}
