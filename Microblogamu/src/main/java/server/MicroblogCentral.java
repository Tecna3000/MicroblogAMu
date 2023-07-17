package server;

import Database.Database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MicroblogCentral {
    private final static ArrayBlockingQueue<MicroblogHandler> queue = new ArrayBlockingQueue<>(20);
    public static void main(String[] args) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newWorkStealingPool();
        ServerSocket serverSocket = new ServerSocket(12345);
        Database database = new Database("src/main/resources/database.db");
        database.connect();
        database.init();
        while (true) {
            Socket socket = serverSocket.accept();
            MicroblogHandler microblogHandler = new MicroblogHandler(socket, database);
            queue.add(microblogHandler);
            executorService.submit(queue.take());
        }
    }
}
