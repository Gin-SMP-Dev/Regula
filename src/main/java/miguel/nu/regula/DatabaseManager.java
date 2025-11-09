package miguel.nu.regula;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static Connection connection;
    private final static String url = "jdbc:mysql://localhost:3306/gin"; // move to .env later im lazy this is just temp
    private final static String username = "root"; // move to .env later im lazy this is just temp
    private final static String password = ""; // move to .env later im lazy this is just temp

    public static void connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("MySQL connection established!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("MySQL connection closed!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
