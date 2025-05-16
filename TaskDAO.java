import java.util.List;

public interface TaskDAO {
    List<Task> findAll();
    void addTask(Task task);
    void markTaskCompleted(int id);
    void deleteTask(int id);
}
