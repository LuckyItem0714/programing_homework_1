import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class TitleSortStrategyTest {
    @Test
    public void testSortByTitle() {
        List<Task> tasks = Arrays.asList(
            new Task(1, "数学", "算数", "2025-06-01", false),
            new Task(2, "英語", "英語", "2025-06-02", false)
        );
        TitleSortStrategy strategy = new TitleSortStrategy();
        List<Task> sorted = strategy.sort(tasks);
        assertEquals("英語", sorted.get(0).getTitle());
        assertEquals("数学", sorted.get(1).getTitle());
    }
}
