package Database;

import java.sql.*;

public class Database {

    private final String dbPath;
    private Connection connection;
    private Statement statement;

    public Database(String dbPath) {
        this.dbPath = dbPath;
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            statement = connection.createStatement();
            System.out.println("Connection to SQLite has been established.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        try {
            statement.execute("CREATE TABLE IF NOT EXISTS messages (id LONG PRIMARY KEY NOT NULL UNIQUE, author VARCHAR(255), content TEXT, tags VARCHAR(255), replyTo LONG, republished BOOLEAN)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeStatement(String query) throws SQLException {
        statement.execute(query);
    }

    public synchronized long setMessageId() throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages ORDER BY id DESC LIMIT 1");
        if (resultSet.next()) {
            return resultSet.getLong("id") + 1;
        }
        return 0;
    }

    public boolean messageIdExist(int messageId) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages WHERE id = " + messageId);
        return resultSet.next();
    }

    public String getAuthor(int messageId) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT author FROM messages WHERE id = " + messageId);
        return resultSet.getString("author");
    }

    public String getMessage(int messageId) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT content FROM messages WHERE id = " + messageId);
        return resultSet.getString("content");
    }

    public boolean getRepublished(int messageId) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT republished FROM messages WHERE id = " + messageId);
        return resultSet.getBoolean("republished");
    }

    public Object getReplyTo(int messageId) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT replyTo FROM messages WHERE id = " + messageId);
        return resultSet.getObject("replyTo");
    }

    public String getLastMessageId(int n) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages ORDER BY id DESC LIMIT " + n);
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getLong("id")).append(",");
        }
        return stringBuilder.toString();
    }

    public String getLastMessageOfUser(int n, String user) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages WHERE author = '" + user + "' ORDER BY id DESC LIMIT " + n);
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getLong("id")).append(",");
        }
        return stringBuilder.toString();
    }

    public String getLastMessageOfTag(int n, String tag) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages WHERE tags LIKE '%" + tag + "%' ORDER BY id DESC LIMIT " + n);
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getLong("id")).append(",");
        }
        return stringBuilder.toString();
    }

    public String getLastMessageIdSince(int n, long id) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages WHERE id > " + id + " ORDER BY id DESC LIMIT " + n);
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getLong("id")).append(",");
        }
        return stringBuilder.toString();
    }

    public String getLastMessageOfUserAndTag(int n, String user, String tag) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages WHERE author = '" + user + "' AND tags LIKE '%" + tag + "%' ORDER BY id DESC LIMIT " + n);
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getLong("id")).append(",");
        }
        return stringBuilder.toString();
    }

    public String getLastMessageOfUserSince(int n, String user, long id) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages WHERE author = '" + user + "' AND id > " + id + " ORDER BY id DESC LIMIT " + n);
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getLong("id")).append(",");
        }
        return stringBuilder.toString();
    }

    public String getLastMessageOfTagSince(int n, String tag, long id) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages WHERE tags LIKE '%" + tag + "%' AND id > " + id + " ORDER BY id DESC LIMIT " + n);
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getLong("id")).append(",");
        }
        return stringBuilder.toString();
    }

    public String getLastMessageOfUserAndTagSince(int n, String user, String tag, long id) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT id FROM messages WHERE author = '" + user + "' AND tags LIKE '%" + tag + "%' AND id > " + id + " ORDER BY id DESC LIMIT " + n);
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getLong("id")).append(",");
        }
        return stringBuilder.toString();
    }

    public String getTags(int id) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT tags FROM messages WHERE id = " + id);
        return resultSet.getString("tags");
    }

    public int getNumberOfMessageOfAuthor(String author) throws SQLException {
        String query = "SELECT COUNT(*) FROM messages WHERE author = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, author);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }


}
