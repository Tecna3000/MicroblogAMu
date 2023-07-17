package flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.TimerTask;

public class Subscribe extends TimerTask {
    private final String command;
    private Socket socket;

    public Subscribe(String command, Socket socket) {
        this.command = command;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            if (command.startsWith("author:")) {
                System.out.println("----------------------------------------");
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));;
                String[] authorsArray = command.split("@");
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
                System.out.println("----------------------------------------");
            } else if (command.startsWith("tag:")){

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
