import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskDAO dao = new TaskDAOImpl();

        // 課題を追加
        dao.addTask(new Task(0, "レポート提出", "情報処理", "2025-05-20", false));
        dao.addTask(new Task(0, "プレゼン準備", "英語プレゼン", "2025-05-25", false));

        // 課題一覧を表示
        List<Task> tasks = dao.findAll();
        for (Task task : tasks) {
            System.out.println("ID: " + task.getId());
            System.out.println("課題名: " + task.getTitle());
            System.out.println("授業名: " + task.getSubject());
            System.out.println("締め切り: " + task.getDeadline());
            System.out.println("状態: " + (task.isCompleted() ? "完了" : "未完了"));
            System.out.println("---------------------------");
        }

        // 課題を完了にマーク
        dao.markTaskCompleted(1);

        // 課題を削除
        dao.deleteTask(2);
    }
}
