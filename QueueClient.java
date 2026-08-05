import java.io.*;
import java.net.*;
import java.util.Scanner;

public class QueueClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8888)) {
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
            
            System.out.print("Enter command (e.g., GET_Q or CHECK_Q): ");
            String command = scanner.nextLine();
            
            out.println(command); 
            
            String response = in.readLine(); 
            System.out.println("[CLIENT] Received response: " + response);
            
        } catch (IOException e) {
            System.out.println("Cannot connect to Server (Is Server running?)");
        }
    }
}