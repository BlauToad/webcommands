package de.blautoad.webcommands;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Listener {
    static ServerSocket serverSocket;

    public static void m() throws InterruptedException {
        while (true) {
            try {
                Listener.serverSocket = new ServerSocket(Config.getPort());
                while (!Listener.serverSocket.isClosed()) {
                    listenToClientConnections();
                }
            } catch (Exception ignored) {
                System.out.println(ignored.getMessage());
                Thread.sleep(1000);
            }
        }
    }

    private static void listenToClientConnections() throws IOException {
        Socket clientSocket = Listener.serverSocket.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        String requestLine = in.readLine();
        if (requestLine == null) {
            return;
        }
        String[] requestParts = requestLine.split(" ");
        String method = requestParts[0];
        String url = requestParts[1];

        System.out.println(method);
        System.out.println(url);
        System.out.println(String.join(",", requestParts));
        if (Objects.equals(method, "OPTIONS")) {
            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
            outputStream.write("Access-Control-Allow-Origin: *\r\n".getBytes());
            outputStream.write("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n".getBytes());
            outputStream.write("Access-Control-Allow-Headers: content-type\r\n".getBytes());
            outputStream.write("\r\n".getBytes());
        }
        if (Objects.equals(method, "GET")) {
            if (url.startsWith("/get")) {
                GET(clientSocket, url);
            } else if (url.startsWith("/builder")) {
                GET_builder(clientSocket);
            } else if (url.startsWith("/post")) {
                GET_post(clientSocket, url);
            } else {
                GET_old(clientSocket, url);
            }
        }
        if (Objects.equals(method, "POST")) {
            // Read headers and find Content-Length
            int contentLength = 0;
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    break; // End of headers
                }
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                }
            }

            // Read the request body
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                String requestBody = new String(body);

                System.out.println("Request Body: " + requestBody);

                Gson gson = new Gson();
                String[] tasks = gson.fromJson(URLDecoder.decode(requestBody, StandardCharsets.UTF_8), String[].class);

                System.out.println("Request Body: " + String.join(",", tasks));

                POST(clientSocket, tasks);
            }

        }

        // Close the Web Connection
        clientSocket.close();
    }

    private static void GET_builder(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

        String response = WebcommandsClient.getBuilder_html();

        // Response for the Web-Request
        out.print("HTTP/1.1 200 OK\n");
        out.print("Access-Control-Allow-Origin: *\r\n");
        out.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        out.print("Access-Control-Allow-Headers: content-type\r\n");
        out.print("Content-Length: " + response.length() + "\n");
        out.print("Content-Type: text/html; charset=utf-8\n");
        out.print("Date: Tue, 25 Oct 2016 08:17:59 GMT\n");
        out.print("\n");
        out.print(response);
        out.flush();

    }

    private static void GET(Socket clientSocket, String url) throws IOException {

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
        String response = "{\"commandResult\":\"*NO RESPONSE\",\"commandResultText\":\"*NO RESPONSE\",\"debug\":-1}";

        //get the command localhost:42069?/command => execute "/command" & localhost:42069?text => send "text" to chat!
        String[] a = url.split("\\?");

        StringBuilder b = new StringBuilder();
        for (int i = 1; i < a.length; i++) {
            if (i > 1) {
                b.append("?");
            }
            b.append(a[i]);
        }
        String[] c = b.toString().split(" ");
        if (c.length == 0) {
            return;
        }
        String d = c[0];
        String e = URLDecoder.decode(d, StandardCharsets.UTF_8);

        e = e.replace("\\\\$%", "#");


        if (MinecraftClient.getInstance().player != null) {
            try {
                String t = new Gson().fromJson(e, String.class);
                //filter empty messages:
                if (t != null && !Objects.equals(t, "")) {
                    if (t.startsWith("/")) {
                        // Executing the Command:
                        MinecraftClient.getInstance().player.networkHandler.sendChatCommand(t.substring(1));

                        Result r = ResultManager.waitForResult_ObjFilter(t.split(" ")[0].substring(1), 5, TimeUnit.SECONDS);


                        response = new Gson().toJson(r);
                    } else {
                        // Sending the Message in the Chat:
                        MinecraftClient.getInstance().player.networkHandler.sendChatMessage(t);

                    }
                }

            } catch (Exception ignored) {
                System.out.println("Error:" + ignored.getMessage());
            }
        } else {
            response = "{\"commandResult\":\"*EXECUTION NOT POSSIBLE\",\"commandResultText\":\"*EXECUTION NOT POSSIBLE\",\"debug\":-2}";
        }

        // Response for the Web-Request
        out.print("HTTP/1.1 200 OK\n");
        out.print("Access-Control-Allow-Origin: *\r\n");
        out.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        out.print("Access-Control-Allow-Headers: content-type\r\n");
        out.print("Content-Length: " + response.length() + "\n");
        out.print("Content-Type: application/json; charset=utf-8\n");
        out.print("Date: Tue, 25 Oct 2016 08:17:59 GMT\n");
        out.print("\n");
        out.print(response);
        out.flush();

    }

    private static void GET_old(Socket clientSocket, String url) throws IOException {

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

        //get the command localhost:42069?/command => execute "/command" & localhost:42069?text => send "text" to chat!
        String[] a = url.split("\\?");

        StringBuilder b = new StringBuilder();
        for (int i = 1; i < a.length; i++) {
            if (i > 1) {
                b.append("?");
            }
            b.append(a[i]);
        }
        String[] c = b.toString().split(" ");
        if (c.length == 0) {
            return;
        }
        String d = c[0];
        String e = URLDecoder.decode(d, StandardCharsets.UTF_8);

        e = e.replace("\\\\$%", "#");

        int delay = 0;
        boolean comments = false;


        if (MinecraftClient.getInstance().player != null) {
            try {
                String[] tasks = new Gson().fromJson(e, String[].class);
                if (tasks != null) {
                    for (String t : tasks) {
                        //filter empty messages:
                        if (!Objects.equals(t, "")) {
                            if (t.startsWith("/&timeout ")) {
                                try {
                                    int timeout = Integer.parseInt(t.substring(10).replace(" ", ""));
                                    if (timeout > 1) {
                                        Thread.sleep(timeout);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                                continue;
                            }
                            if (t.startsWith("/&setdelay ")) {
                                try {
                                    delay = Integer.parseInt(t.substring(11).replace(" ", ""));
                                } catch (NumberFormatException ignored) {
                                }
                                continue;
                            }
                            if (!comments && t.equals("/&enableComments")) {
                                comments = true;
                                continue;
                            }
                            if (delay > 0) {
                                Thread.sleep(delay);
                            }

                            if (t.startsWith("/")) {
                                // Executing the Command:
                                MinecraftClient.getInstance().player.networkHandler.sendChatCommand(t.substring(1));

                            } else {
                                if (!(comments && t.startsWith("#"))) {
                                    // Sending the Message in the Chat:
                                    MinecraftClient.getInstance().player.networkHandler.sendChatMessage(t);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                System.out.println("Error:" + ignored.getMessage());
            }
        }

        // Response for the Web-Request
        String response = "<!DOCTYPE html><html lang=\"en\"><head> <meta charset=\"UTF-8\"> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> <title>Webcommands</title> <style> textarea { margin: 1vw; width: 96vw; min-height: 50vh; } </style></head><body> <h4>Deprecated! Use the <a href=\"./builder\">New Command Builder</a></h4><br><br><p> Custom commands:<br> /&timeout <b>[time]</b>: This command allows you to set a pause for a specified duration. For instance, /&timeout <b>5000</b> would result in a pause of <b>5 seconds</b>.<br> /&setdelay <b>[time]</b>: This command enables you to set a delay after each command. For example, /&setdelay <b>500</b> would introduce a delay of <b>500 milliseconds, or half a second</b>, after each command. </p> <textarea id=\"in\" oninput=\"inputHandler();\" style=\"resize:vertical;\" autocomplete=\"off\" spellcheck=\"false\"></textarea> <label for=\"comments\">Dont execute lines that start with # </label> <input type=\"checkbox\" id=\"comments\" onchange=\"inputHandler();\" /><br><br> <a id=\"out\"></a> <script> window.history.replaceState({ id: 'Webcommands', source: 'JS' }, 'Webcommands', window.location.origin + window.location.pathname); let in_ = document.getElementById(\"in\"); let out_ = document.getElementById(\"out\"); let form_ = document.getElementById(\"form\"); let comments_ = document.getElementById(\"comments\"); let old_data = \"" + d + "\"; in_.value = JSON.parse(decodeURI(old_data)).join([separator = '\\n']).replaceAll(\"\\\\$%\",\"#\"); function inputHandler() { var spl = in_.value.split(\"\\n\"); if(spl[0] == \"/&enableComments\"){ comments_.checked = true; spl = spl.reverse(); spl.pop(); spl = spl.reverse(); in_.value = spl.join([separator = '\\n']) } if (comments_.checked) { spl = spl.reverse(); spl.push(\"/&enableComments\"); spl = spl.reverse(); } var val = window.location.origin + window.location.pathname + \"?\" + encodeURI(JSON.stringify(spl).replaceAll(\"#\", \"\\\\\\\\$%\")); out_.innerHTML = val; out_.href = val; } inputHandler(); </script></body></html>";
        out.print("HTTP/1.1 200 OK\n");
        out.print("Content-Length: " + response.length() + "\n");
        out.print("Content-Type: text/html; charset=utf-8\n");
        out.print("Date: Tue, 25 Oct 2016 08:17:59 GMT\n");
        out.print("\n");
        out.print(response);
        out.flush();

    }

    private static void GET_post(Socket clientSocket, String url) throws IOException {

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

        //get the command localhost:42069?/command => execute "/command" & localhost:42069?text => send "text" to chat!
        String[] a = url.split("\\?");

        StringBuilder b = new StringBuilder();
        for (int i = 1; i < a.length; i++) {
            if (i > 1) {
                b.append("?");
            }
            b.append(a[i]);
        }
        String[] c = b.toString().split(" ");
        if (c.length == 0) {
            return;
        }
        String d = c[0];
        String e = URLDecoder.decode(d, StandardCharsets.UTF_8);

        e = e.replace("\\\\$%", "#");

        int delay = 0;

        if (MinecraftClient.getInstance().player != null) {
            try {
                String[] tasks = new Gson().fromJson(e, String[].class);
                if (tasks != null) {
                    for (String t1 : tasks) {
                        String t = "/" + t1;
                        //filter empty messages:
                        if (!Objects.equals(t, "/")) {
                            if (t.startsWith("/&timeout ")) {
                                try {
                                    int timeout = 0;
                                    if (t.endsWith("ms")) {
                                        timeout = Integer.parseInt(t.substring(11, t.length() - 2).replace(" ", ""));
                                    } else {
                                        timeout = Integer.parseInt(t.substring(10).replace(" ", ""));
                                    }
                                    if (timeout > 1) {
                                        Thread.sleep(timeout);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                                continue;
                            }
                            if (t.startsWith("/&setdelay ")) {
                                try {
                                    if (t.endsWith("ms")) {
                                        delay = Integer.parseInt(t.substring(11, t.length() - 2).replace(" ", ""));
                                    } else {
                                        delay = Integer.parseInt(t.substring(11).replace(" ", ""));
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                                continue;
                            }
                            if (delay > 0) {
                                Thread.sleep(delay);
                            }

                            if (t.startsWith("/&chat")) {
                                // Sending the Message in the Chat:
                                MinecraftClient.getInstance().player.networkHandler.sendChatMessage(t.substring(7));

                            } else {
                                if (!t.startsWith("#")) {
                                    // Executing the Command:
                                    MinecraftClient.getInstance().player.networkHandler.sendChatCommand(t.substring(1));
                                }

                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                System.out.println("Error:" + ignored.getMessage());
            }
        }
        // Response for the Web-Request
        String response = new Gson().toJson("ok");
        out.print("HTTP/1.1 200 OK\n");
        out.print("Access-Control-Allow-Origin: *\r\n");
        out.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        out.print("Access-Control-Allow-Headers: content-type\r\n");
        out.print("Content-Length: " + response.length() + "\n");
        out.print("Content-Type: text/html; charset=utf-8\n");
        out.print("Date: Tue, 25 Oct 2016 08:17:59 GMT\n");
        out.print("\n");
        out.print(response);
        out.flush();

    }

    private static void POST(Socket clientSocket, String[] tasks) throws IOException {

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

        // Response for the Web-Request
        String response = new Gson().toJson("ok");
        out.print("HTTP/1.1 200 OK\n");
        out.print("Access-Control-Allow-Origin: *\r\n");
        out.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        out.print("Access-Control-Allow-Headers: content-type\r\n");
        out.print("Content-Length: " + response.length() + "\n");
        out.print("Content-Type: text/html; charset=utf-8\n");
        out.print("Date: Tue, 25 Oct 2016 08:17:59 GMT\n");
        out.print("\n");
        out.print(response);
        out.flush();

        int delay = 0;

        if (MinecraftClient.getInstance().player != null) {
            try {
                if (tasks != null) {
                    for (String t1 : tasks) {
                        String t = "/" + t1;
                        //filter empty messages:
                        if (!Objects.equals(t, "/")) {
                            if (t.startsWith("/&timeout ")) {
                                try {
                                    int timeout = 0;
                                    if (t.endsWith("ms")) {
                                        timeout = Integer.parseInt(t.substring(11, t.length() - 2).replace(" ", ""));
                                    } else {
                                        timeout = Integer.parseInt(t.substring(10).replace(" ", ""));
                                    }
                                    if (timeout > 1) {
                                        Thread.sleep(timeout);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                                continue;
                            }
                            if (t.startsWith("/&setdelay ")) {
                                try {
                                    if (t.endsWith("ms")) {
                                        delay = Integer.parseInt(t.substring(11, t.length() - 2).replace(" ", ""));
                                    } else {
                                        delay = Integer.parseInt(t.substring(11).replace(" ", ""));
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                                continue;
                            }
                            if (delay > 0) {
                                Thread.sleep(delay);
                            }

                            if (t.startsWith("/&chat")) {
                                // Sending the Message in the Chat:
                                MinecraftClient.getInstance().player.networkHandler.sendChatMessage(t.substring(7));

                            } else {
                                if (!t.startsWith("#")) {
                                    // Executing the Command:
                                    MinecraftClient.getInstance().player.networkHandler.sendChatCommand(t.substring(1));
                                }

                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                System.out.println("Error:" + ignored.getMessage());
            }
        }

    }

    private static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static void restartServerSocket() {
        try {
            if (Listener.serverSocket != null && !Listener.serverSocket.isClosed()) {
                Listener.serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
