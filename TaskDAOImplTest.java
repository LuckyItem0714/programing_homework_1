import org.junit.*;
import java.sql.*;
import java.util.List;
import static org.junit.Assert.*;

public class TaskDAOImplTest {

    private TaskDAOImpl dao;

    @Before
    public void setUp() throws Exception {
        DBUtil.setConnection("jdbc:sqlite::memory:");

        // テーブル作成（TaskDAOImplのコンストラクタでも作ってるが念のため）
        try (Statement stmt = DBUtil.getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "subject TEXT," +
                "deadline TEXT," +
                "completed BOOLEAN)");
        }

        dao = new TaskDAOImpl();
    }

    @After
    public void tearDown() throws Exception {
        DBUtil.closeConnection();
    }

    @Test
    public void testAddAndFindAll() {
        Task task = new Task(0, "Test Task", "A", "2025-06-01", false);
        dao.addTask(task);

        List<Task> tasks = dao.findAll();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }

    @Test
    public void testMarkTaskCompleted() {
        Task task = new Task(0, "Complete Me", "B",  "2025-06-02", false);
        dao.addTask(task);

        List<Task> tasks = dao.findAll();
        int id = tasks.get(0).getId();

        dao.markTaskCompleted(id);
        Task updated = dao.findAt(id);
        assertTrue(updated.isCompleted());
    }

    @Test
    public void testDeleteTask() {
        Task task = new Task(0, "Delete Me", "C", "2025-06-03", false);
        dao.addTask(task);

        List<Task> tasks = dao.findAll();
        int id = tasks.get(0).getId();

        dao.deleteTask(id);
        Task deleted = dao.findAt(id);
        assertNull(deleted);
    }
}
