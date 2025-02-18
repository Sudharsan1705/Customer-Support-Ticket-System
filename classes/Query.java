import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class Query extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection con = null;
	     try {
	            con = JDBCConnection.getConnection();

				
				int id = 0;

				HttpSession session = request.getSession(false);
				id = (int)(session.getAttribute("id"));

				String userName = (String)session.getAttribute("name");
				
				
				String query = request.getParameter("discuss");
				
				String s = "insert into disscussion (ticket_id,user_name,query,query_time) values(?,?,?,NOW())";
	            PreparedStatement statement = con.prepareStatement(s);
				statement.setInt(1, id);
				statement.setString(2, userName);
				statement.setString(3, query);
				int cnt = statement.executeUpdate();
				
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                con.close();
	            } catch (SQLException e) {
	            	e.printStackTrace();
	            }
	        }
	}

}
