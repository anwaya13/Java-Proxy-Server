import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class MainServer {
    public static void main(String[] args) {
        try {
            ServerSocket mainServerSocket = new ServerSocket(8080,50,InetAddress.getByName("0.0.0.0"));
            System.out.println("MainServer is running...");
            while (true) {
                Socket connectionSocket = mainServerSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                String request = inFromClient.readLine();
                if (request != null && request.contains("GET /index.html")) {
                    System.out.println("MainServer received request for index.html");
                    sendIndexFile("index.html", outToClient);
                } else if (request != null && request.contains("GET /index2.html")) {
                    System.out.println("MainServer received request for index2.html");
                    sendIndexFile("index2.html", outToClient);
                }
                connectionSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void sendIndexFile(String fileName, DataOutputStream outToClient) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            outToClient.writeBytes("HTTP/1.1 200 OK\r\n");
            outToClient.writeBytes("Content-Length: " + bytes.length + "\r\n");
            outToClient.writeBytes("Content-Type: text/html\r\n");
            outToClient.writeBytes("\r\n");
            outToClient.write(bytes, 0, bytes.length);
            fileInputStream.close();
        } else {
            // If file not found, send 404 error
            String errorMessage = "<h1>404 Not Found</h1>";
            outToClient.writeBytes("HTTP/1.1 404 Not Found\r\n");
            outToClient.writeBytes("Content-Length: " + errorMessage.length() + "\r\n");
            outToClient.writeBytes("Content-Type: text/html\r\n");
            outToClient.writeBytes("\r\n");
            outToClient.writeBytes(errorMessage);
        }
    }
}
