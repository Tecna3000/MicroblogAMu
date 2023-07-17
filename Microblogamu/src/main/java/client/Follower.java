package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Follower {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket("localhost", 12345);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Enter author or authors of message you want to see id message: ");
        String authors = scanner.nextLine();
        String[] authorsArray = authors.split("@");
        for (String author : authorsArray) {
            if (author.equals("")) {
                continue;
            }
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("RCV_IDS author:@" + author);
            String response = in.readLine();
            String[] messageLines = response.split("\\\\r\\\\n|,");
            for (int i = 1; i < messageLines.length; i++) {
                socket = new Socket("localhost", 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println("RCV_MSG msg_id:" + messageLines[i]);
                String responseMessage = in.readLine();
                String[] messageLinesMessage = responseMessage.split("\\\\r\\\\n");
                System.out.println(messageLinesMessage[0]);
                for (int j = 1; j < messageLinesMessage.length; j++) {
                    System.out.println(messageLinesMessage[j] + " ");
                }
            }
        }

    }
}
