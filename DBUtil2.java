import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil2 {
    private static String url = "jdbc:sqlite:tasks.db";
    private static Connection conn;

    public static void setConnection(String newUrl) throws SQLException {
        url = newUrl;
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        conn = DriverManager.getConnection(url);
    }

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(url);
        }
        return conn;
    }

    public static void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
