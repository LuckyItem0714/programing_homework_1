import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAOImpl implements TaskDAO {

    public TaskDAOImpl() {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "title TEXT," +
                         "subject TEXT," +
                         "deadline TEXT," +
                         "completed BOOLEAN)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Task task = new Task(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("subject"),
                    rs.getString("deadline"),
                    rs.getBoolean("completed")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    @Override
    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (title, subject, deadline, completed) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getSubject());
            pstmt.setString(3, task.getDeadline());
            pstmt.setBoolean(4, task.isCompleted());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void markTaskCompleted(int id) {
        String sql = "UPDATE tasks SET completed = 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
