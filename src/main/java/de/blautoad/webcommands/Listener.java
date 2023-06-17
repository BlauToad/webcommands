package de.blautoad.webcommands;

import net.minecraft.client.MinecraftClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Listener {
    static ServerSocket serverSocket;

    public static void m() {
        while(true){
            try {
                serverSocket = new ServerSocket(Config.getPort());
                while(!serverSocket.isClosed()) {
                    listenToClientConnections(serverSocket);
                }
            }catch(IOException ignored){}
        }
    }

    private static void listenToClientConnections(ServerSocket serverSocket) throws IOException {
        Socket clientSocket = serverSocket.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String requestedResource = "";
        String incomingLineFromClient;
        while ((incomingLineFromClient = in.readLine()) != null) {
            //System.out.println(incomingLineFromClient);

            if(incomingLineFromClient.contains("HTTP/1.1")) {
                requestedResource = incomingLineFromClient;
            }

            if (incomingLineFromClient.equals(""))
                break;
        }

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

        //get the command localhost:42069?/command => execute "/command" & localhost:42069?text => send "text" to chat!
        String[] a = requestedResource.split("\\?");
        StringBuilder b = new StringBuilder();
        for(int i = 1; i < a.length; i++) {
            if(i > 1) {
                b.append("?");
            }
            b.append(a[i]);
        }
        String[] c = b.toString().split(" ");
        String d = c[0];
        d = URLDecoder.decode(d, StandardCharsets.UTF_8);

        String response_color = "red";

        //filter empty messages:
        if(!Objects.equals(d, "")){
            if(MinecraftClient.getInstance().player != null){
                if(d.startsWith("/")){
                    // Executing the Command:
                    try{
                        MinecraftClient.getInstance().player.networkHandler.sendChatCommand(d.substring(1));
                        response_color = "#fb0;";
                    }catch(Exception ignored){}
                }else{
                    // Sending the Message in the Chat:
                    try{
                        MinecraftClient.getInstance().player.networkHandler.sendChatMessage(d);
                        response_color = "#0b0;";
                    }catch(Exception ignored){}
                }
            }
        }

        // Response for the Web-Request
        String response = "<!DOCTYPE html><html lang=\"en\"><body> <a>Received:</a> <br> <a style=\"color: " + response_color + ";\">" + escapeHTML(d) + "</a> <br> <br> <textarea id=\"in\" oninput=\"inputHandler();\" style=\"width:99%; resize:vertical;\">" + escapeHTML(d) + "</textarea> <br> <a>Click this link to execute:</a> <br> <a id=\"out\">" + escapeHTML(d) + "</a> <script> let in_ = document.getElementById(\"in\"); let out_ = document.getElementById(\"out\"); function inputHandler() { in_.value = in_.value.replace(\"\\n\",\"\"); out_.innerHTML = in_.value; out_.href = \"?\" + encodeURI(in_.value); } inputHandler(); </script></body></html>";
        out.print("HTTP/1.1 200 OK\n");
        out.print("Content-Length: " + response.length() + "\n");
        out.print("Content-Type: text/html; charset=utf-8\n");
        out.print("Date: Tue, 25 Oct 2016 08:17:59 GMT\n");
        out.print("\n");
        out.print(response);
        out.flush();

        // Close the Web Connection
        clientSocket.close();
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

    public static void restartServerSocket(){
        try {
            if(!serverSocket.isClosed()){
                serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
