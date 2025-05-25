import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import java.sql.*;
import java.util.List;

import static org.junit.Assert.*;

public class TaskDAOImplTest {

    private TaskDAOImpl dao;

    @Before
    public void setUp() throws SQLException {
        DBUtil.setConnection("jdbc:sqlite::memory:"); 
        dao = new TaskDAOImpl();
    }

        @After
    public void tearDown() {
        dao.close(); 
    }
    
    @Test
    public void testAddAndFindAll() {
        Task task = new Task(0, "task1", "A", "2025-06-01", false);
        dao.addTask(task);

        List<Task> tasks = dao.findAll();
        assertEquals(1, tasks.size());
        assertEquals("task1", tasks.get(0).getTitle());
    }

    @Test
    public void testFindAt() {
        Task task = new Task(0, "task2", "B", "2025-06-02", false);
        dao.addTask(task);

        int id = dao.findAll().get(0).getId();
        Task result = dao.findAt(id);

        assertNotNull(result);
        assertEquals("B", result.getSubject());
    }

    @Test
    public void testMarkTaskCompleted() {
        Task task = new Task(0, "task3", "C", "2025-06-02", false);
        dao.addTask(task);

        int id = dao.findAll().get(0).getId();
        dao.markTaskCompleted(id);

        Task updated = dao.findAt(id);
        assertTrue(updated.isCompleted());
    }

    @Test
    public void testDeleteTask() {
        Task task = new Task(0, "task4", "D", "2025-06-03", false);
        dao.addTask(task);

        int id = dao.findAll().get(0).getId();
        dao.deleteTask(id);

        Task deleted = dao.findAt(id);
        assertNull(deleted);
    }
}
