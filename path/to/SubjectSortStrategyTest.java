import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

public class SubjectSortStrategyTest {

    @Test
    public void testSubjectSortStrategy() {
        SortStrategy strategy = new SubjectSortStrategy();

        Task t1 = new Task(1, "課題1", "情報処理", "2025-06-01", false);
        Task t2 = new Task(2, "課題2", "英語プレゼン", "2025-06-02", false);
        Task t3 = new Task(3, "課題3", "ウェブインテリジェンス", "2025-06-03", false);
        Task t4 = new Task(4, "課題4", "プログラミング応用", "2025-06-04", false);
        Task t5 = new Task(5, "課題5", "情報ネットワーク", "2025-06-05", false);

        List<Task> tasks = Arrays.asList(t1, t2, t3, t4, t5);
        List<Task> sorted = strategy.sort(tasks);

        List<String> expectedOrder = Arrays.asList(
            "ウェブインテリジェンス",
            "プログラミング応用",
            "英語プレゼン",
            "情報ネットワーク",
            "情報処理"
        );

        for (int i = 0; i < sorted.size(); i++) {
            assertEquals(expectedOrder.get(i), sorted.get(i).getSubject());
        }
    }
}
