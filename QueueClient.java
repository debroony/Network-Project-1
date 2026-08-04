import java.io.*;
import java.net.*;
import java.util.Scanner;

public class QueueClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8888)) {
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
            
            System.out.print("พิมพ์คำสั่งขอคิว (เช่น GET_Q 2): ");
            String command = scanner.nextLine();
            
            out.println(command); 
            
            String response = in.readLine(); 
            System.out.println("[CLIENT] ได้รับข้อความ: " + response);
            
        } catch (IOException e) {
            System.out.println("ไม่สามารถเชื่อมต่อ Server ได้ (เปิด Server หรือยัง?)");
        }
    }
}