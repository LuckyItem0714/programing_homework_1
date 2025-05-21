import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTest {
    private Task task;

    @Before
    public void setUp() {
        task = new Task(1, "レポート", "プログラミング応用", "2025-05-26", false);
    }

    @Test
    public void testGetters() {
        assertEquals(1, task.getId());
        assertEquals("レポート", task.getTitle());
        assertEquals("プログラミング応用", task.getSubject());
        assertEquals("2025-05-26", task.getDeadline());
        assertFalse(task.isCompleted());
    }

    @Test
    public void testSetters() {
        task.setId(2);
        task.setTitle("課題");
        task.setSubject("アルゴリズム");
        task.setDeadline("2025-06-01");
        task.setCompleted(true);

        assertEquals(2, task.getId());
        assertEquals("課題", task.getTitle());
        assertEquals("アルゴリズム", task.getSubject());
        assertEquals("2025-06-01", task.getDeadline());
        assertTrue(task.isCompleted());
    }
}
