import java.util.List;

public interface TaskDAO {
    List<Task> findAll();
    void showAll();
    void addTask(Task task);
    void markTaskCompleted(int id);
    void deleteTask(int id);
    int select();
}
