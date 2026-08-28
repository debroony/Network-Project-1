import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.LinkedList;
import java.util.Queue;

public class QueueServer {

    private static int vA = 0, vB = 0, vC = 0;
    private static int nA = 0, nB = 0, nC = 0;
    
    private static final Queue<Integer> qVA = new LinkedList<>();
    private static final Queue<Integer> qVB = new LinkedList<>();
    private static final Queue<Integer> qVC = new LinkedList<>();
    private static final Queue<Integer> qNA = new LinkedList<>();
    private static final Queue<Integer> qNB = new LinkedList<>();
    private static final Queue<Integer> qNC = new LinkedList<>();
    
    private static final String ADMIN_PASS = "admin6767"; 
    
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("[Server] Smart Queue Protocol (SQP) running on Port 8888");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] New Client Connected: " + socket.getInetAddress());
                pool.execute(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String request;
                while ((request = in.readLine()) != null) {
                    if (request.equals("EXIT")) {
                        System.out.println("[Server] Client Disconnected.");
                        break; 
                    }
                    
                    System.out.println("[Server] Raw Request Received: " + request);

                    String cmd = "", type = "NORMAL", pass = "", size = "1 person";
                    String[] parts = request.split("\\|");
                    for (String part : parts) {
                        String[] kv = part.split(":");
                        if (kv.length == 2) {
                            if (kv[0].equals("CMD")) cmd = kv[1];
                            if (kv[0].equals("TYPE")) type = kv[1];
                            if (kv[0].equals("PASS")) pass = kv[1];
                            if (kv[0].equals("SIZE")) size = kv[1];
                        }
                    }

                    if (cmd.equals("AUTH")) {
                        if (pass.equals(ADMIN_PASS)) {
                            out.println("STATUS:200|Q_NO:-|MSG:Authentication Successful");
                        } else {
                            out.println("STATUS:401|Q_NO:-|MSG:Unauthorized - Invalid Password");
                        }
                    }
                    else if (cmd.equals("GET_Q")) {
                        String qNo = "";
                        synchronized (QueueServer.class) {
                            if (type.equals("VIP")) {
                                if (size.contains("1")) { vA++; qNo = String.format("VA-%03d", vA); qVA.add(vA); }
                                else if (size.contains("2-4")) { vB++; qNo = String.format("VB-%03d", vB); qVB.add(vB); }
                                else if (size.contains("6-8")) { vC++; qNo = String.format("VC-%03d", vC); qVC.add(vC); }
                            } else {
                                if (size.contains("1")) { nA++; qNo = String.format("NA-%03d", nA); qNA.add(nA); }
                                else if (size.contains("2-4")) { nB++; qNo = String.format("NB-%03d", nB); qNB.add(nB); }
                                else if (size.contains("6-8")) { nC++; qNo = String.format("NC-%03d", nC); qNC.add(nC); }
                            }
                        }
                        out.println("STATUS:201|Q_NO:" + qNo + "|MSG:Queue Created Successfully (" + size + ")");
                    } 
                    else if (cmd.equals("NEXT_Q")) {
                        if (!pass.equals(ADMIN_PASS)) {
                            out.println("STATUS:401|Q_NO:-|MSG:Unauthorized - Invalid Admin Password");
                            continue;
                        }

                        String qNo = "-";
                        String msg = "";
                        synchronized (QueueServer.class) {
                            if (type.equals("VIP")) {
                                if (size.contains("1") && !qVA.isEmpty()) { qNo = String.format("VA-%03d", qVA.poll()); msg = "Calling VIP (1 pax)"; }
                                else if (size.contains("2-4") && !qVB.isEmpty()) { qNo = String.format("VB-%03d", qVB.poll()); msg = "Calling VIP (2-4 pax)"; }
                                else if (size.contains("6-8") && !qVC.isEmpty()) { qNo = String.format("VC-%03d", qVC.poll()); msg = "Calling VIP (6-8 pax)"; }
                                else { msg = "No VIP queue waiting for size: " + size; }
                            } else {
                                if (size.contains("1") && !qNA.isEmpty()) { qNo = String.format("NA-%03d", qNA.poll()); msg = "Calling Normal (1 pax)"; }
                                else if (size.contains("2-4") && !qNB.isEmpty()) { qNo = String.format("NB-%03d", qNB.poll()); msg = "Calling Normal (2-4 pax)"; }
                                else if (size.contains("6-8") && !qNC.isEmpty()) { qNo = String.format("NC-%03d", qNC.poll()); msg = "Calling Normal (6-8 pax)"; }
                                else { msg = "No Normal queue waiting for size: " + size; }
                            }
                        }
                        int status = qNo.equals("-") ? 404 : 200;
                        out.println("STATUS:" + status + "|Q_NO:" + qNo + "|MSG:" + msg);
                    }
                    else if (cmd.equals("CHECK_Q")) {
                        String vipSummary = String.format("VIP [1 pax: %d, 2-4 pax: %d, 6-8 pax: %d]", qVA.size(), qVB.size(), qVC.size());
                        String normalSummary = String.format("NORMAL [1 pax: %d, 2-4 pax: %d, 6-8 pax: %d]", qNA.size(), qNB.size(), qNC.size());
                        out.println("STATUS:200|Q_NO:-|MSG:Waiting -> " + vipSummary + " | " + normalSummary);
                    }
                    else if (cmd.equals("ESTIMATE_TIME")) {
                        int vipWait = (qVA.size() + qVB.size() + qVC.size()) * 2;       
                        int normalWait = (qNA.size() + qNB.size() + qNC.size()) * 5; 
                        out.println("STATUS:200|Q_NO:-|MSG:Est. Time -> VIP: " + vipWait + " mins, NORMAL: " + normalWait + " mins");
                    }
                    else {
                        out.println("STATUS:400|Q_NO:-|MSG:Bad Request - Invalid Command");
                    }
                }
            } catch (IOException e) {
                System.out.println("[Server-Thread] Connection Error or Client Dropped");
            }
        }
    }
}