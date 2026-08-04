import java.io.*;
import java.net.*;

public class QueueServer {
    public static void main(String[] args) {
            int currentQueue = 0;

        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("[Server] ระบบจองคิวเปิดทำงานแล้ว! รอรับคำสั่งที่ Port 8888");

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] มีผู้ใช้เชื่อมต่อเข้ามาแล้ว");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String request = in.readLine();
                System.out.println("[Server] รับคำสั่ง: " + request);

                if (request == null) {
                    out.println("400 BAD REQUEST ไม่พบคำสั่ง");
                }
                else if (request.equalsIgnoreCase("GET_Q")) {
                    currentQueue++;
                    String formattedQueue = String.format("%03d", currentQueue);
                    out.println("201 CREATED ได้คิวแล้ว! คิวของคุณคือ Q-" + formattedQueue);
                }
                else if (request.equalsIgnoreCase("CHECK_Q")) {
                    String formattedQueue = String.format("%03d", currentQueue);
                    out.println("200 OK คิวปัจจุบันคือ Q-" + formattedQueue);
                }
                else {
                    out.println("400 BAD REQUEST คำสั่งไม่ถูกต้อง - กรุณาใช้ GET_Q หรือ CHECK_Q");
                }

                socket.close();
            }
        
        } catch (IOException e) {
                    e.printStackTrace();
        }
    } 
}