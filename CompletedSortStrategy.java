import java.util.List;
import java.util.Comparator;

public class CompletedSortStrategy implements SortStrategy {
    @Override
    public List<Task> sort(List<Task> tasks) {
        tasks.sort(Comparator.comparing(Task::isCompleted));
        return tasks;
    }
}
