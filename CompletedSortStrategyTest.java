import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class CompletedSortStrategyTest {
    @Test
    public void testSortByCompletion() {
        List<Task> tasks = Arrays.asList(
            new Task(1, "A", "国語", "2025-06-10", true),
            new Task(2, "B", "理科", "2025-05-01", false)
        );
        CompletedSortStrategy strategy = new CompletedSortStrategy();
        List<Task> sorted = strategy.sort(tasks);
        assertFalse(sorted.get(0).isCompleted());
        assertTrue(sorted.get(1).isCompleted());
    }
}
