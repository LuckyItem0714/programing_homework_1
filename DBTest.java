import java.sql.*;
public class DBTest{
    public static void main(String args[]){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection con;
            con = DriverManager.getConnection("jdbc:sqlite:pro4db");
            PreparedStatement pstmt =
                con.prepareStatement("select * from account where id=?");
            pstmt.setInt(1,1);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                System.out.println(rs.getString("name")+":"+rs.getInt("balance"));
            }
            rs.close();pstmt.close();con.close();
        }catch(Exception e){e.printStackTrace();}
    }
}
