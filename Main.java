import java.util.List;

public class Main {
	public static void main(String[] args) {
		TaskDAO dao = new TaskDAOImpl();
		dao.showAll();
		while (true) {
			int check=dao.select();
			if(check==1) {
				break;
			}
		}
		
	}
}
