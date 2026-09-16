package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static String url = "jdbc:mysql://127.0.0.1:3306/campus_resource_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "Pass@123";
    private static String lastErrorMessage = "";

    static {
        loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void loadProperties() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (!propFile.exists()) {
            propFile = new File("../db.properties");
        }
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
                url = props.getProperty("db.url", url);
                user = props.getProperty("db.user", user);
                password = props.getProperty("db.password", password);
            } catch (Exception ignored) {}
        } else {
            try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    url = props.getProperty("db.url", url);
                    user = props.getProperty("db.user", user);
                    password = props.getProperty("db.password", password);
                }
            } catch (Exception ignored) {}
        }
    }

    public static void setCredentials(String newUrl, String newUser, String newPassword) {
        if (newUrl != null && !newUrl.trim().isEmpty()) url = newUrl.trim();
        if (newUser != null && !newUser.trim().isEmpty()) user = newUser.trim();
        if (newPassword != null) password = newPassword.trim();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            lastErrorMessage = "";
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {}
            }
        }
    }
}
