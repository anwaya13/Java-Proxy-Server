import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class ProxyServer {
    static boolean firstAccess = true;

    public static void main(String[] args) {
        try {
            ServerSocket proxyServerSocket = new ServerSocket(8888,50, InetAddress.getByName("0.0.0.0"));
            System.out.println("ProxyServer is running...");
            while (true) {
                Socket connectionSocket = proxyServerSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                String request = inFromClient.readLine();
                if (request != null && (request.contains("GET /index.html") || request.contains("GET /index2.html"))) {
                    String requestedFile = request.contains("GET /index.html") ? "index.html" : "index2.html";
                    System.out.println("ProxyServer received request for " + requestedFile);
                    if (firstAccess) {
                        System.out.println("First access, getting page from MainServer...");
                        Socket mainServerSocket = new Socket("localhost", 8080);
                        BufferedReader inFromMainServer = new BufferedReader(new InputStreamReader(mainServerSocket.getInputStream()));
                        DataOutputStream outToMainServer = new DataOutputStream(mainServerSocket.getOutputStream());
                        outToMainServer.writeBytes("GET /" + requestedFile + " HTTP/1.1\r\n");
                        outToMainServer.writeBytes("\r\n");
                        StringBuilder responseBuilder = new StringBuilder();
                        String line;
                        while ((line = inFromMainServer.readLine()) != null) {
                            responseBuilder.append(line).append("\n");
                        }
                        firstAccess = false;
                        System.out.println("ProxyServer caching " + requestedFile + "...");
                        FileWriter fileWriter = new FileWriter("cached_pages/" + requestedFile);
                        fileWriter.write(responseBuilder.toString());
                        fileWriter.close();

                        outToClient.writeBytes(responseBuilder.toString());
                        mainServerSocket.close();
                    } else {
                        System.out.println("Returning cached " + requestedFile + "...");
                        BufferedReader cachedPageReader = new BufferedReader(new FileReader("cached_pages/" + requestedFile));
                        StringBuilder responseBuilder = new StringBuilder();
                        String line;
                        while ((line = cachedPageReader.readLine()) != null) {
                            responseBuilder.append(line).append("\n");
                        }
                        outToClient.writeBytes(responseBuilder.toString());
                        cachedPageReader.close();
                    }
                }
                connectionSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
