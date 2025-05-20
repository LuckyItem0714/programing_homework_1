import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:sqlite:tasks.db");
	}
}
