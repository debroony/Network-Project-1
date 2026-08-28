import java.io.*;
import java.net.*;
import java.util.Scanner;

public class QueueClient {
    public static void main(String[] args) {
        try (
            Socket socket = new Socket("localhost", 8888);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("=== Welcome to Smart Queue System ===");
            System.out.println("Select your role:");
            System.out.println("1. Customer (User)");
            System.out.println("2. Counter Staff (Admin)");
            System.out.print("Role (1 or 2): ");
            String role = scanner.nextLine();
            
            boolean isAdmin = false;
            String adminPass = "";

            if (role.equals("2")) {
                System.out.print("Enter Admin Password: ");
                adminPass = scanner.nextLine();
                
                String authReq = "CMD:AUTH|TYPE:ADMIN|PASS:" + adminPass;
                out.println(authReq);
                String authRes = in.readLine();
                
                if (authRes != null && authRes.contains("STATUS:200")) {
                    System.out.println("\n[System] Login Success! Access Granted.");
                    isAdmin = true;
                } else {
                    System.out.println("\n[System] Login Failed! Incorrect password. Exiting program...");
                    return;
                }
            }

            while (true) {
                String choice;
                String request = "";

                if (!isAdmin) {
                    System.out.println("\n--- Customer Menu ---");
                    System.out.println("1. Request VIP Queue");
                    System.out.println("2. Request Normal Queue");
                    System.out.println("3. Check Waiting Queues");
                    System.out.println("4. Estimate Waiting Time");
                    System.out.println("0. Exit");
                    System.out.print("Select menu: ");
                    choice = scanner.nextLine();
                    
                    if (choice.equals("1") || choice.equals("2")) {
                        System.out.println("\nSelect Party Size:");
                        System.out.println("A. 1 person");
                        System.out.println("B. 2-4 persons");
                        System.out.println("C. 6-8 persons");
                        System.out.print("Choice (A/B/C): ");
                        String sizeChoice = scanner.nextLine().toUpperCase();
                        
                        String sizeVal = "1 person";
                        if (sizeChoice.equals("B")) sizeVal = "2-4 persons";
                        else if (sizeChoice.equals("C")) sizeVal = "6-8 persons";

                        String type = choice.equals("1") ? "VIP" : "NORMAL";
                        request = "CMD:GET_Q|TYPE:" + type + "|SIZE:" + sizeVal;
                    }
                    else if (choice.equals("3")) request = "CMD:CHECK_Q|TYPE:ALL";
                    else if (choice.equals("4")) request = "CMD:ESTIMATE_TIME|TYPE:ALL"; 
                    else if (choice.equals("0")) {
                        out.println("EXIT");
                        System.out.println("Goodbye!");
                        break;
                    } else {
                        System.out.println("Invalid choice.");
                        continue;
                    }
                } else {
                    System.out.println("\n--- Counter Staff Menu ---");
                    System.out.println("1. Call Next VIP Queue");
                    System.out.println("2. Call Next Normal Queue");
                    System.out.println("3. Check Waiting Queues");
                    System.out.println("0. Exit");
                    System.out.print("Select menu: ");
                    choice = scanner.nextLine();

                    if (choice.equals("1") || choice.equals("2")) {
                        System.out.println("\nSelect Table Size to Call:");
                        System.out.println("A. 1 person");
                        System.out.println("B. 2-4 persons");
                        System.out.println("C. 6-8 persons");
                        System.out.print("Choice (A/B/C): ");
                        String sizeChoice = scanner.nextLine().toUpperCase();
                        
                        String sizeVal = "1 person";
                        if (sizeChoice.equals("B")) sizeVal = "2-4 persons";
                        else if (sizeChoice.equals("C")) sizeVal = "6-8 persons";

                        String type = choice.equals("1") ? "VIP" : "NORMAL";
                        
                        request = "CMD:NEXT_Q|TYPE:" + type + "|SIZE:" + sizeVal + "|PASS:" + adminPass;
                    }
                    else if (choice.equals("3")) request = "CMD:CHECK_Q|TYPE:ALL";
                    else if (choice.equals("0")) {
                        out.println("EXIT");
                        System.out.println("Logging off. Goodbye!");
                        break;
                    } else {
                        System.out.println("Invalid choice.");
                        continue;
                    }
                }

                System.out.println("\n[CLIENT] Sending Protocol Message: " + request);
                out.println(request); 
                
                String response = in.readLine(); 
                if (response == null) {
                    System.out.println("Server disconnected abruptly.");
                    break;
                }

                System.out.println("[CLIENT] Raw Response Received: " + response);
                
                System.out.println("--- Decoded Message ---");
                String[] respParts = response.split("\\|");
                for (String part : respParts) {
                    System.out.println(" > " + part);
                }
                System.out.println("-----------------------");
            }

        } catch (IOException e) {
            System.out.println("Cannot connect to Server. (Is Server running?)");
        }
    }
}