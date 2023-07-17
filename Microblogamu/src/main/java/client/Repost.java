package client;

import Database.Database;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;

public class Repost {

    public static void main(String[] args) throws IOException, SQLException {
        Database database = new Database("src/main/resources/database.db");
        database.connect();
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket("localhost", 12345);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Enter your pseudo: ");
        String user = scanner.nextLine();
        System.out.println("Enter author or authors of you want to repost message: ");
        String authors = scanner.nextLine();
        String[] authorsArray = authors.split("@");
        for (String author : authorsArray) {
            if (author.equals("")) {
                continue;
            }
            int numberOfMessage = database.getNumberOfMessageOfAuthor("@" + author);
            out.println("RCV_IDS author:@" + author + " limit:" + numberOfMessage);
            String response = in.readLine();
            String[] messageLines = response.split("\\\\r\\\\n|,");
            for (int i = 1; i < messageLines.length; i++) {
                out.println("REPUBLISH author:" + user + " msg_id:" + messageLines[i]);
            }
        }
        database.close();
    }
}