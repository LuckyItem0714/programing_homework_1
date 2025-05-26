import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import java.util.ArrayList;
import java.util.List;

public class TaskSorterTest {
    private TaskSorter sorter;
    private List<Task> tasks;

    @Before
    public void setUp() {
        sorter = new TaskSorter();
        tasks = new ArrayList<>();
        tasks.add(new Task(1, "Task C", "Subject C", "2023-12-01", false));
        tasks.add(new Task(2, "Task A", "Subject A", "2023-11-01", true));
        tasks.add(new Task(3, "Task B", "Subject B", "2023-10-01", false));
    }

    @After
    public void tearDown() {
        sorter = null;
        tasks = null;
    }

    @Test
    public void testSortByTitle() {
        sorter.setStrategy(new TitleSortStrategy());
        List<Task> sortedTasks = sorter.sortTasks(tasks);
        Assert.assertEquals("Task A", sortedTasks.get(0).getTitle());
        Assert.assertEquals("Task B", sortedTasks.get(1).getTitle());
        Assert.assertEquals("Task C", sortedTasks.get(2).getTitle());
    }

    @Test
    public void testSortByCompleted() {
        sorter.setStrategy(new CompletedSortStrategy());
        List<Task> sortedTasks = sorter.sortTasks(tasks);
        Assert.assertFalse(sortedTasks.get(0).isCompleted());
        Assert.assertFalse(sortedTasks.get(1).isCompleted());
        Assert.assertTrue(sortedTasks.get(2).isCompleted());
    }

    public void testSortByDeadline() {
        sorter.setStrategy(new DeadlineSortStrategy());
        List<Task> sortedTasks = sorter.sortTasks(tasks);
        Assert.assertEquals("Subject A", sortedTasks.get(0).getDeadline());
        Assert.assertEquals("Subject B", sortedTasks.get(1).getDeadline());
        Assert.assertEquals("Subject C", sortedTasks.get(2).getDeadline());
    }
    
    public void testSortBySubject() {
        sorter.setStrategy(new SubjectSortStrategy());
        List<Task> sortedTasks = sorter.sortTasks(tasks);
        Assert.assertEquals("2023-10-01", sortedTasks.get(0).getSubject());
        Assert.assertEquals("2023-11-01", sortedTasks.get(1).getSubject());
        Assert.assertEquals("2023-12-01", sortedTasks.get(2).getSubject());
    }
}
