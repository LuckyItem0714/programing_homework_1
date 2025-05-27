import java.text.Collator;
import java.util.List;
import java.util.Locale;

public class TitleSortStrategy implements SortStrategy {
	@Override
	public List<Task> sort(List<Task> tasks) {
		Collator collator = Collator.getInstance(Locale.JAPANESE);
		tasks.sort((t1, t2) -> collator.compare(t1.getTitle(), t2.getTitle()));
		return tasks;
	}
}
