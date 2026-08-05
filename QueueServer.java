import java.io.*;
import java.net.*;

public class QueueServer {
    public static void main(String[] args) {
        int currentQueue = 0;

        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("[Server] Queue System is running on Port 8888");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] Client connected.");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String request = in.readLine();
                System.out.println("[Server] Received command: " + request);

                if (request == null) {
                    out.println("400 BAD REQUEST - Request is empty");
                } 
                else if (request.equalsIgnoreCase("GET_Q")) {
                    currentQueue++;
                    String formattedQueue = String.format("%03d", currentQueue);
                    out.println("201 CREATED - Your queue number is Q-" + formattedQueue);
                } 
                else if (request.equalsIgnoreCase("CHECK_Q")) {
                    String formattedQueue = String.format("%03d", currentQueue);
                    out.println("200 OK - Current queue is Q-" + formattedQueue);
                } 
                else {
                    out.println("400 BAD REQUEST - Invalid command. Please use GET_Q or CHECK_Q");
                }

                socket.close();
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
}