
public class Task {
    private int id;
    private String title;
    private String subject;
    private String deadline;
    private boolean completed;


    public Task(int id, String title, String subject, String deadline, boolean completed) {
        this.id = id;
        this.title = title;
        this.subject=subject;
        this.deadline=deadline;
        this.completed=completed;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getDeadline() { return deadline;}
    public boolean isCompleted() { return completed; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDeadline(String deadline) { this.deadline = deadline; } 
    public void setCompleted(boolean completed) { this.completed = completed; }

}
