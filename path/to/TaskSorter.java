import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskSorter {
	private SortStrategy strategy;
	public void setStrategy(SortStrategy strategy) {
		this.strategy = strategy;
	}
	
	public List<Task> sortTasks(List<Task> tasks) {
		if (strategy == null) {
			throw new IllegalStateException("ソート戦略が設定されていません");
		}
		return strategy.sort(tasks);
	}
}
