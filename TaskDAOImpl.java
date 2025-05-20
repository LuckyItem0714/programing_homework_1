import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaskDAOImpl implements TaskDAO {

	public TaskDAOImpl() {
		try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "title TEXT,"
					+ "subject TEXT," + "deadline TEXT," + "completed BOOLEAN)");
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
				Task task = new Task(rs.getInt("id"), rs.getString("title"), rs.getString("subject"),
						rs.getString("deadline"), rs.getBoolean("completed"));
				tasks.add(task);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tasks;
	}
	
	@Override
	public void showAll() {
		List<Task> showtasks = findAll();
		for (Task task : showtasks) {
			System.out.println("ID: " + task.getId());
			System.out.println("課題名: " + task.getTitle());
			System.out.println("授業名: " + task.getSubject());
			System.out.println("締め切り: " + task.getDeadline());
			System.out.println("状態: " + (task.isCompleted() ? "完了" : "未完了"));
			System.out.println("---------------------------");
		}
	}

	@Override
	public void addTask(Task task) {
		String sql = "INSERT INTO tasks (title, subject, deadline, completed) VALUES (?, ?, ?, ?)";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteTask(int id) {
		String sql = "DELETE FROM tasks WHERE id = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int select() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("操作を選択してください　1：課題の追加、2：課題の完了、3：課題の削除、4：一覧の表示、5：終了");
		int selnum = scanner.nextInt();
		switch (selnum) {
		case 1:
			System.out.print("課題名：");
			String title = scanner.next();
			System.out.print("授業名：");
			String subject = scanner.next();
			System.out.println("締め切り：");
			System.out.print("年：");
			int year = scanner.nextInt();
			System.out.print("月：");
			int month = scanner.nextInt();
			System.out.print("日：");
			int day = scanner.nextInt();
			String deadline=year+"-"+month+"-"+day;
			addTask(new Task(0, title, subject, deadline, false));
			return 0;
		case 2:
			System.out.print("完了した課題のid：");
			int finishedid = scanner.nextInt();
			markTaskCompleted(finishedid);
			return 0;
		case 3:
			System.out.print("削除する課題のid：");
			int deleteid = scanner.nextInt();
			deleteTask(deleteid);
			return 0;
		case 4:
			showAll();
			return 0;
		case 5:
			return 1;
		default:
			System.out.println("1から5の数字を入力してください");
			return 0;
		}
	}
}
