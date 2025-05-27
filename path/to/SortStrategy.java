import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface SortStrategy {
	List<Task> sort(List<Task> tasks);
}
