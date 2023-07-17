package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Publisher {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        Scanner scanner = new Scanner(System.in);
        String pseudo;

        do {
            System.out.print("Enter your pseudo (alphanumeric characters only, no spaces): ");
            pseudo = scanner.nextLine();
        } while (!pseudo.matches("^[a-zA-Z0-9]+$"));

        pseudo = "author:@" + pseudo;

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String message = scanner.nextLine();
        out.println("PUBLISH " + pseudo + "\\r\\n" + message + "\\r\\n");

        String response = in.readLine();
        String[] messageLines = response.split("\\\\r\\\\n| ");
        String command = messageLines[0];

        switch (command) {
            case "OK" -> System.out.println("Message published");
            case "ERROR" -> {
                StringBuilder errorMessage = new StringBuilder();
                for (int i = 1; i < messageLines.length; i++) {
                    errorMessage.append(messageLines[i]).append(" ");
                }
                System.out.println(messageLines[0] + ": " + errorMessage);
            }
        }

    }

}
