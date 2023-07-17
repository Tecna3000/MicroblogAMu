package server;

import Database.Database;
import flux.Subscribe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Timer;

public class MicroblogHandler implements Runnable {

    private final Socket socket;
    private final Database database;

    private static final int MAX_MESSAGE_SIZE = 256;

    public MicroblogHandler(Socket socket, Database database) {
        this.socket = socket;
        this.database = database;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String message = in.readLine();
            String[] messageLines = message.split("\\\\r\\\\n| ");
            String command = messageLines[0];

            switch (command) {
                case "PUBLISH" -> {
                    String author = messageLines[1].split(":")[1];
                    int contentMessageSize = (message.length() - (messageLines[0].length() + messageLines[1].length() + 9));
                    if (contentMessageSize > MAX_MESSAGE_SIZE) {
                        out.println("ERROR\\r\\nMessage too long\\r\\n");
                        out.close();
                        in.close();
                        return;
                    }
                    StringBuilder completeMessage = new StringBuilder();
                    StringBuilder tags = new StringBuilder();
                    for (int i = 2; i < messageLines.length; i++) {
                        completeMessage.append(messageLines[i]).append(" ");
                        if (messageLines[i].startsWith("#")) {
                            tags.append(messageLines[i]).append(",");
                        }
                    }
                    database.executeStatement("INSERT INTO messages (id, author, content, tags, replyTo, republished) VALUES (" + database.setMessageId() + ", '" + author + "', '" + completeMessage + "', '" + tags + "', NULL, 0)");
                    System.out.println(author + " " + completeMessage);
                    out.println("OK\\r\\n\\r\\n");
                    out.close();
                    in.close();
                }
                case "RCV_IDS" -> {
                    String user = null;
                    String tag = null;
                    long id = -1;
                    int n = 5;

                    for (int i = 1; i < messageLines.length; i++) {
                        if (messageLines[i].startsWith("author:")) {
                            user = messageLines[i].split(":")[1];
                        } else if (messageLines[i].startsWith("tag:")) {
                            tag = messageLines[i].split(":")[1];
                        } else if (messageLines[i].startsWith("since_id:")) {
                            id = Long.parseLong(messageLines[i].split(":")[1]);
                        } else if (messageLines[i].startsWith("limit:")) {
                            n = Integer.parseInt(messageLines[i].split(":")[1]);
                        }
                    }

                    if (user == null && tag == null && id == -1) {
                        out.println("MSG_IDS\\r\\n" + database.getLastMessageId(n) + "\\r\\n");
                        out.close();
                        in.close();
                    }
                    if (user != null && tag == null && id == -1) {
                        out.println("MSG_IDS\\r\\n" + database.getLastMessageOfUser(n, user) + "\\r\\n");
                        out.close();
                        in.close();
                    }
                    if (user == null && tag != null && id == -1) {
                        out.println("MSG_IDS\\r\\n" + database.getLastMessageOfTag(n, tag) + "\\r\\n");
                        out.close();
                        in.close();
                    }
                    if (user == null && tag == null && id != -1) {
                        out.println("MSG_IDS\\r\\n" + database.getLastMessageIdSince(n, id) + "\\r\\n");
                        out.close();
                        in.close();
                    }
                    if (user != null && tag != null && id == -1) {
                        out.println("MSG_IDS\\r\\n" + database.getLastMessageOfUserAndTag(n, user, tag) + "\\r\\n");
                        out.close();
                        in.close();
                    }
                    if (user != null && tag == null && id != -1) {
                        out.println("MSG_IDS\\r\\n" + database.getLastMessageOfUserSince(n, user, id) + "\\r\\n");
                        out.close();
                        in.close();
                    }
                    if (user == null && tag != null && id != -1) {
                        out.println("MSG_IDS\\r\\n" + database.getLastMessageOfTagSince(n, tag, id) + "\\r\\n");
                        out.close();
                        in.close();
                    }
                    if (user != null && tag != null && id != -1) {
                        out.println("MSG_IDS\\r\\n" + database.getLastMessageOfUserAndTagSince(n, user, tag, id) + "\\r\\n");
                        out.close();
                        in.close();
                    }

                    out.close();
                    in.close();
                }
                case "RCV_MSG" -> {
                    if (!messageLines[1].split(":")[0].equals("msg_id")) {
                        out.println("ERROR\\r\\nBad request format. The good format is : RCV_MSG msg_id:id\\r\\n");
                        out.close();
                        in.close();
                        return;
                    }
                    int messageId = Integer.parseInt(messageLines[1].split(":")[1]);
                    if (database.messageIdExist(messageId)) {
                        if (database.getReplyTo(messageId) == null) {
                            out.println("MSG " + database.getAuthor(messageId) + " [republished:" + database.getRepublished(messageId) + "]" + "\\r\\n" + database.getMessage(messageId) + "\\r\\n");
                        } else {
                            out.println("MSG " + database.getAuthor(messageId) + " [reply_to_id:" + database.getReplyTo(messageId) + "] [republished:" + database.getRepublished(messageId) + "]" + "\\r\\n" + database.getMessage(messageId) + "\\r\\n");
                        }
                    } else {
                        out.println("ERROR\\r\\nMessage id not exist\\r\\n");
                    }
                    out.close();
                    in.close();
                }
                case "REPUBLISH" -> {
                    if (messageLines.length < 3) {
                        out.println("ERROR\\r\\nBad request format. The good format is : REPUBLISH author:@user msg_id:id\\r\\n");
                        out.close();
                        in.close();
                        return;
                    }
                    if ((!messageLines[1].startsWith("author:")) && (!messageLines[2].startsWith("msg_id:"))) {
                        out.println("ERROR\\r\\nBad request format. The good format is : REPUBLISH author:@user msg_id:id\\r\\n");
                        out.close();
                        in.close();
                        return;
                    }
                    String author = messageLines[1].split(":")[1];
                    int id = Integer.parseInt(messageLines[2].split(":")[1]);
                    if (database.messageIdExist(id)) {
                        String messageToRepublish = database.getMessage(id);
                        database.executeStatement("INSERT INTO messages (id, author, content, tags, replyTo, republished) VALUES (" + database.setMessageId() + ", '" + author + "', '" + messageToRepublish + "', '" + database.getTags(id) + "', NULL, " + 1 + ")");
                        out.println("OK\\r\\n\\r\\n");
                    } else {
                        out.println("ERROR\\r\\nMessage id not exist\\r\\n");
                    }
                    out.close();
                    in.close();
                }
                case "REPLY" -> {
                    if (messageLines.length < 3) {
                        out.println("ERROR\\r\\nBad request format. The good format is : entete : REPLY author:@user reply_to_id:id corps : contenu du message\\r\\n");
                        out.close();
                        in.close();
                        return;
                    }
                    if ((!messageLines[1].startsWith("author:")) && (!messageLines[2].startsWith("reply_to_id:"))) {
                        out.println("ERROR\\r\\nBad request format. The good format is : REPLY author:@user reply_to_id:id\\r\\n");
                        out.close();
                        in.close();
                        return;
                    }
                    String author = messageLines[1].split(":")[1];
                    int replyTo = Integer.parseInt(messageLines[2].split(":")[1]);
                    if (database.messageIdExist(replyTo)) {
                        int contentMessageSize = (message.length() - (messageLines[0].length() + messageLines[1].length() + messageLines[2].length() + 10));
                        if (contentMessageSize > MAX_MESSAGE_SIZE) {
                            out.println("ERROR\\r\\nMessage too long\\r\\n");
                            out.close();
                            in.close();
                            return;
                        }
                        StringBuilder completeMessage = new StringBuilder();
                        StringBuilder tags = new StringBuilder();
                        for (int i = 3; i < messageLines.length; i++) {
                            completeMessage.append(messageLines[i]).append(" ");
                            if (messageLines[i].startsWith("#")) {
                                tags.append(messageLines[i]).append(",");
                            }
                        }
                        database.executeStatement("INSERT INTO messages (id, author, content, tags, replyTo, republished) VALUES (" + database.setMessageId() + ", '" + author + "', '" + completeMessage + "', '" + tags + "', " + replyTo + ", 0)");
                        out.println("OK\\r\\n\\r\\n");
                    } else {
                        out.println("ERROR\\r\\nMessage id not exist\\r\\n");
                    }
                    out.close();
                    in.close();
                }
                case "SUBSCRIBE" -> {
                    if(messageLines[1].startsWith("author:") || messageLines[1].startsWith("tag:")){
                        Subscribe subscribe = new Subscribe(messageLines[1], socket);
                        Timer timer = new Timer();
                        timer.schedule(subscribe, 0, (5 *1000));
                        out.println("OK\\r\\n\\r\\n");
                    } else {
                        out.println("ERROR\\r\\nBad request format. The good format is : SUBSCRIBE author:@user or SUBSCRIBE tag:#tag\\r\\n");
                    }
                }
                default -> {
                    out.println("ERROR\\r\\nUnknown command\\r\\n");
                    out.close();
                    in.close();
                }
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}

