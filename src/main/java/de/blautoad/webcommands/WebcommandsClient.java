package de.blautoad.webcommands;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;

@Environment(EnvType.CLIENT)
public class WebcommandsClient implements ClientModInitializer {

    private static String builder_html;

    public static String getBuilder_html() {
        return builder_html;
    }

    public WebcommandsClient() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("index.html");

        StringBuilder contentBuilder = new StringBuilder();
        if (inputStream == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            } catch (Exception ignored) {
            }
        }

        builder_html = contentBuilder.toString();
    }

    @Override
    public void onInitializeClient() {

        boolean port_available = false;
        // Testing if the default port is free
        try (ServerSocket serverSocket1 = new ServerSocket(Config.getPort())) {
            // port 80 is available
            serverSocket1.close();
            port_available = true;
        } catch (IOException ignored) {
            int[] fbp = Config.getFallbackPorts();
            boolean fbp_available = false;
            for (int i = 0; i < fbp.length && !fbp_available; i++) {
                // Testing if the default port is free
                try (ServerSocket serverSocket2 = new ServerSocket(fbp[i])) {
                    // port 80 is available
                    serverSocket2.close();
                    port_available = true;
                    fbp_available = true;
                    Config.tempChangePort(fbp[i]);
                } catch (IOException ignored2) {
                }
            }
            if (!fbp_available) {
                // port is already used
                try (ServerSocket serverSocket3 = new ServerSocket(0)) {
                    // a port is available
                    Config.tempChangePort(serverSocket3.getLocalPort());
                    serverSocket3.close();
                    port_available = true;
                } catch (IOException ignored3) {
                }
            }

        }

        if (port_available) {
            // Starting a Thread that handles the Web interface
            Thread thread = new Thread(() -> {
                try {
                    Listener.m();
                } catch (Exception ignored) {
                }
            });
            thread.start();
        }//TODO: Error, no Port is available on this machine!

    }


}
