import java.io.*;
import java.net.*;

public class QueueServer {
    private static int currentQueue = 0;
    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("[Server] Queue System is running on Port 8888");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] Client connected.");

                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 

    private static class ClientHandler extends Thread {
            private Socket socket;

            public ClientHandler(Socket socket) {
                this.socket = socket;
            }
            public void run() {
                try { 
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String request = in.readLine();
                System.out.println("[Server] Received command: " + request);

                if (request == null) {
                    out.println("400 BAD REQUEST - Request is empty");
                } 
                else if (request.equalsIgnoreCase("GET_Q")) {
                    synchronized (QueueServer.class) {
                        currentQueue++;
                        String formattedQueue = String.format("%03d", currentQueue);
                        out.println("201 CREATED - Your queue number is Q-" + formattedQueue);
                    }
                } 
                else if (request.equalsIgnoreCase("CHECK_Q")) {
                    String formattedQueue = String.format("%03d", currentQueue);
                    out.println("200 OK - Current queue is Q-" + formattedQueue);
                } 
                else {
                    out.println("400 BAD REQUEST - Invalid command. Please use GET_Q or CHECK_Q");
                }

                socket.close();
                
            } catch (IOException e) {
                System.out.println("[Server-Thread] Connection Error");
            }
        }
    }
}