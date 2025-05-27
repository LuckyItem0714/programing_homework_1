import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class DeadlineSortStrategyTest {
    @Test
    public void testSortByDeadline() {
        List<Task> tasks = Arrays.asList(
            new Task(1, "A", "国語", "2025-06-10", false),
            new Task(2, "B", "理科", "2025-05-01", false)
        );
        DeadlineSortStrategy strategy = new DeadlineSortStrategy();
        List<Task> sorted = strategy.sort(tasks);
        assertEquals("2025-05-01", sorted.get(0).getDeadline());
    }
}
