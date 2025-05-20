import java.util.List;

public interface TaskDAO {
    List<Task> findAll();
    void show(List<Task> tasks);
    void showAll();
    void addTask(Task task);
    void markTaskCompleted(int id);
    void deleteTask(int id);
    String setDate();
    int select();
}
