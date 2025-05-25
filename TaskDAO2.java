import java.util.List;

public interface TaskDAO2 {
    List<Task> findAll();
    void show(List<Task> tasks);
    void showAll();
    void addTask(Task task);
    void markTaskCompleted(int id);
    void deleteTask(int id);
    String setDate();
    int select();
    Task findAt(int id);
}
