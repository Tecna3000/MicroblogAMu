package server;

import Database.Database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newWorkStealingPool();
        ServerSocket serverSocket = new ServerSocket(12345);
        Database database = new Database("src/main/resources/database.db");
        database.connect();
        database.init();
        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(socket, database);
            executorService.submit(clientHandler);
        }
    }

}
